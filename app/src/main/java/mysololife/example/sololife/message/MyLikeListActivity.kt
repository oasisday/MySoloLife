package mysololife.example.sololife.message

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import com.example.mysololife.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.message.fcm.NotiModel
import mysololife.example.sololife.message.fcm.PushNotification
import mysololife.example.sololife.message.fcm.RetrofitInstance
import mysololife.example.sololife.utils.FBboard
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import mysololife.example.sololife.utils.MyInfo
import okhttp3.Dispatcher
import java.util.UUID
import java.util.Vector

class MyLikeListActivity : AppCompatActivity() {

    private val TAG = "MyLikeListActivity"
    private val uid = FirebaseAuthUtils.getUid()

    private val likeUserList = mutableListOf<UserDataModel>()
    private val likeUserListUid = mutableListOf<String>()

    lateinit var listviewAdapter : ListViewAdapter
    lateinit var getterUid : String
    lateinit var getterToken : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like_list)

        val userListView = findViewById<ListView>(R.id.userListView)
        val writebtn = findViewById<ImageView>(R.id.writeBtn)

        listviewAdapter = ListViewAdapter(this, likeUserList)

        userListView.adapter = listviewAdapter

        //내가 좋아요한 사람들
        getMyLikeList()

        //짧은 클릭
        userListView.setOnItemClickListener{ parent, view, position, id ->

            //checkMatching(likeUserList[position].uid.toString())
            /*
            val notiModel = NotiModel("a", "b")

            val pushModel = PushNotification(notiModel,likeUserList[position].token.toString())
            testPush(pushModel)
            */
        }
        //긴 클릭
        userListView.setOnItemLongClickListener{ parent, view, position, id ->

            getterUid = likeUserList[position].uid.toString()
            getterToken = likeUserList[position].token.toString()
            checkMatching(likeUserList[position].uid.toString())

            return@setOnItemLongClickListener (true)
        }

        writebtn.setOnClickListener{

            val groupId = UUID.randomUUID().toString()

            val groupModel = GroupDataModel(
                null,null, Vector()
            )
            groupModel.groupnum = groupId
            groupModel.leader = uid
            groupModel.member?.add(uid)

            val selectedItems = listviewAdapter.getSelectedItems()
                for(item in selectedItems){
                //    Log.d("강민기",item.uid.toString())
                //    Log.d("강민기",uid)

                    groupModel.member?.add(item.uid.toString())

                }
            FBboard.boardInfoRef.child(groupId).setValue(groupModel)
        }


    }

    private fun checkMatching(otherUid : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.children.count() == 0){
                    Toast.makeText(this@MyLikeListActivity,"매칭 실패", Toast.LENGTH_SHORT).show()
                }else{
                    for (dataModel in dataSnapshot.children){
                        val likeUserKey = dataModel.key.toString()
                        //매칭이 되어있는지
                        if(likeUserKey.equals(uid)){
                            Toast.makeText(this@MyLikeListActivity, "매칭 성공", Toast.LENGTH_SHORT).show()
                            //Dialog
                            showDialog()

                        }else{
                            Toast.makeText(this@MyLikeListActivity,"매칭 실패", Toast.LENGTH_SHORT).show()
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

    private fun getMyLikeList(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){
                    likeUserListUid.add(dataModel.key.toString())
                }

                getUserDataList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)
    }

    private fun getUserDataList() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){

                    val user = dataModel.getValue(UserDataModel::class.java)

                    if(likeUserListUid.contains(user?.uid)){
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

    private  fun testPush(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        RetrofitInstance.api.postNotification(notification)
    }

    private fun showDialog(){

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_msg, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("메세지 보내기")

        val mAlertDialog = mBuilder.show()

        val btn = mAlertDialog.findViewById<Button>(R.id.sendBtn)
        val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)
        btn?.setOnClickListener{

            val msgText = textArea!!.text.toString()

            val msgModel = MsgModel(MyInfo.myNickname, msgText)

            //push를 하면 겹쳐서 계속해서 들어간다//
            FirebaseRef.userMsgRef.child(getterUid).push().setValue(msgModel)

            val notiModel = NotiModel(MyInfo.myNickname, msgText)
            val pushModel = PushNotification(notiModel, getterToken)

            testPush(pushModel)
            mAlertDialog.dismiss()
        }
    }

}