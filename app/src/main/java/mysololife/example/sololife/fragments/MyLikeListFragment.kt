package mysololife.example.sololife.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mysololife.example.sololife.Matching
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.chatlist.ChatActivity
import mysololife.example.sololife.chatlist.ChatRoomItem
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.message.ListViewAdapter
import mysololife.example.sololife.message.MsgModel
import mysololife.example.sololife.message.MyMsgActivity
import mysololife.example.sololife.userlist.UserAdapter
import mysololife.example.sololife.utils.FBboard
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import mysololife.example.sololife.utils.MyInfo
import java.util.*

class MyLikeListFragment : Fragment() {

    private val TAG = "MyLikeListFragment"
    private val uid = FirebaseAuthUtils.getUid()

    private val likeUserList = mutableListOf<UserDataModel>()
    private val likeUserListUid = mutableListOf<String>()

    lateinit var listviewAdapter: ListViewAdapter
    var getterUid: String? = null
    var getterToken: String? = null

    var groupId: String? = null

    lateinit var mBuilder: AlertDialog.Builder
    var mAlertDialog: AlertDialog? = null
    private var groupModel = GroupDataModel(null, null, null, null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_my_like_list, container, false)

        // 툴바 설정 코드를 여기에 추가하세요.
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar) // 툴바 ID를 실제로 사용하는 ID로 변경하세요.
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        toolbar.navigationIcon?.setTint(Color.BLACK)
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userListView = view.findViewById<ListView>(R.id.userListView)
        val writeBtn = view.findViewById<LinearLayout>(R.id.writeBtn)
        val msgBtn = view.findViewById<LinearLayout>(R.id.msgBtn)
        val waitBtn = view.findViewById<LinearLayout>(R.id.waitBtn)

        listviewAdapter = ListViewAdapter(requireContext(), likeUserList) { otherUser ->


            //채팅 연결해 주기
            val myUserId = Firebase.auth.currentUser?.uid ?: ""
            val chatRoomDB = Firebase.database.reference.child(Key.DB_CHAT_ROOMS).child(myUserId)
                .child(otherUser.uid?: "")

            chatRoomDB.get().addOnSuccessListener {
                var chatRoomId = ""
                if (it.value != null) {
                    //데이터가 존재
                    val chatRoom = it.getValue(ChatRoomItem::class.java)
                    chatRoomId = chatRoom?.chatRoomId ?: ""
                } else {
                    chatRoomId = UUID.randomUUID().toString()
                    val newChatRoom = ChatRoomItem(
                        chatRoomId = chatRoomId,
                        otherUserName = otherUser.nickname,
                        otherUserId = otherUser.uid,
                    )
                    chatRoomDB.setValue(newChatRoom)
                }
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otherUser.uid)
                intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId)
                startActivity(intent)
            }
        }
        userListView.adapter = listviewAdapter

        waitBtn.setOnClickListener{
            view?.findNavController()?.navigate(R.id.action_myLikeLikstFragment_to_myLikeWaitFragment)
        }

        msgBtn.setOnClickListener {
            val intent = Intent(context, Matching::class.java)
            startActivity(intent)
        }

        userListView.setOnItemLongClickListener { parent, view, position, id ->
            getterUid = likeUserList[position].uid.toString()
            getterToken = likeUserList[position].token.toString()
            checkMatching(likeUserList[position].uid.toString())
            true
        }



        writeBtn.setOnClickListener {
            groupId = UUID.randomUUID().toString()
            groupModel = GroupDataModel(
                null, null, null, null
            )
            groupModel.groupnum = groupId
            groupModel.leader = uid
            groupModel.member?.add(uid)

            val selectedItems = listviewAdapter.getSelectedItems()
            for (item in selectedItems) {
                groupModel.member?.add(item.uid.toString())
            }

            val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_board, null)
            mBuilder = AlertDialog.Builder(requireContext())
                .setView(mDialogView)
                .setTitle("과목 정보입력")

            mAlertDialog = mBuilder.show()

            val okbtn = mAlertDialog!!.findViewById<Button>(R.id.btnOk)
            val cancelbtn = mAlertDialog!!.findViewById<Button>(R.id.btnCancel)
            val classNameArea = mAlertDialog!!.findViewById<EditText>(R.id.classNameArea)
            val classInfoArea = mAlertDialog!!.findViewById<EditText>(R.id.classInfoArea)

            okbtn?.setOnClickListener {

                val classText = classNameArea!!.text.toString()
                val infoText = classInfoArea!!.text.toString()

                groupModel.classname = classText
                groupModel.classinfo = infoText

                FBboard.boardInfoRef.child(groupId!!).setValue(groupModel)

                Toast.makeText(requireContext(), "스터디 그룹이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                activity?.onBackPressed()
            }
            cancelbtn?.setOnClickListener {
                mAlertDialog!!.dismiss()
            }
        }

        getMyLikeList()
    }

    override fun onResume() {
        super.onResume()
        likeUserList.clear()
    }

    override fun onDestroy() {
        if (mAlertDialog != null && mAlertDialog!!.isShowing) {
            mAlertDialog!!.dismiss()
        }

        super.onDestroy()
    }
    override fun onPause() {
        super.onPause()
        Glide.with(this).pauseAllRequests()
    }

    private fun getMyLikeList() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
                    likeUserListUid.add(dataModel.key.toString())
                }

                getUserDataList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userBothRef.child(uid).addValueEventListener(postListener)

        listviewAdapter.notifyDataSetChanged()
    }

    private fun getUserDataList() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
                    Log.d("whatsproblem",dataModel.toString())
                    val user = dataModel.getValue(UserDataModel::class.java)
                    Log.d("whatsproblem",user!!.nickname.toString() + user!!.toString())
                    if (likeUserListUid.contains(user?.uid)) {
                        //내가 좋아요한 사람들의 정보 뽑음
                        likeUserList.add(user!!)
                    }
                }
                listviewAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)
    }


    private fun checkMatching(otherUid: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.children.count() == 0) {
                    Toast.makeText(requireContext(), "매칭 실패", Toast.LENGTH_SHORT).show()
                } else {
                    for (dataModel in dataSnapshot.children) {
                        val likeUserKey = dataModel.key.toString()
                        //매칭이 되어있는지
                        if (likeUserKey.equals(uid)) {
                            Toast.makeText(requireContext(), "매칭 성공", Toast.LENGTH_SHORT)
                                .show()
                            //Dialog
                            showDialog()

                        } else {
                            Toast.makeText(requireContext(), "매칭 실패", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)
    }

    private fun del(){

        for(tmp in likeUserListUid){
            checkit(tmp)
        }

    }

    //uid의 like에 currentuid가 들어있는지
    private fun checkit(Uid : String) {
        val tmp = mutableListOf<String>()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){
                    val likeUserKey = dataModel.key.toString()
//                    if(likeUserKey == uid){
//                        likeUserListUid.remove(likeUserKey)
//                        Log.d("abcabc",likeUserKey)
//                        return;
//                    }
//                    else{
//                        Log.d("No",likeUserKey)
//                    }

                    tmp.add(dataModel.key.toString())
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userLikeRef.child(Uid).addValueEventListener(postListener)

        if(tmp.contains(uid)){

        }else{
            likeUserListUid.remove(Uid)
        }

    }

    private fun showDialog() {
        val mDialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_msg, null)
        val mBuilder = AlertDialog.Builder(requireContext())
            .setView(mDialogView)
            .setTitle("메세지 보내기")

        val mAlertDialog = mBuilder.show()

        val btn = mAlertDialog.findViewById<Button>(R.id.sendBtn)
        val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)
        val cancelbtn = mAlertDialog.findViewById<Button>(R.id.btnCancel)
        btn?.setOnClickListener {

            val msgText = textArea!!.text.toString()

            val msgModel = MsgModel(MyInfo.myNickname, msgText)

            FirebaseRef.userMsgRef.child(getterUid!!).push().setValue(msgModel)

//            val notiModel = NotiModel(MyInfo.myNickname, msgText)
//            val pushModel = PushNotification(notiModel, getterToken!!)

//            testPush(pushModel)
            mAlertDialog.dismiss()
        }
        cancelbtn!!.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    // Other methods...

    companion object {
        fun newInstance(): MyLikeListFragment {
            return MyLikeListFragment()
        }
    }
}