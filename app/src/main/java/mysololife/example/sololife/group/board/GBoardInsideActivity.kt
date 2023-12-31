package mysololife.example.sololife.group.board

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityBoardInsideBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.auth.UserInfoModel
import mysololife.example.sololife.board.BoardInsideActivity
import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.chatlist.ChatActivity
import mysololife.example.sololife.chatlist.ChatRoomItem
import mysololife.example.sololife.comment.CommentLVAdapter
import mysololife.example.sololife.comment.CommentModel
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBRef
import mysololife.example.sololife.utils.FBboard
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import org.w3c.dom.Comment
import java.util.UUID

class GBoardInsideActivity : Activity() {

    private val TAG = BoardInsideActivity::class.java.simpleName

    private lateinit var binding: ActivityBoardInsideBinding

    private lateinit var key: String

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter: CommentLVAdapter

    private lateinit var title: String
    private lateinit var content: String
    private lateinit var time: String
    private lateinit var uid: String
    private lateinit var bkey: String
    private lateinit var gname: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val commentLV = findViewById<ListView>(R.id.commentLV)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        title = intent.getStringExtra("title").toString()
        content = intent.getStringExtra("content").toString()
        time = intent.getStringExtra("time").toString()
        uid = intent.getStringExtra("uid").toString()
        bkey = intent.getStringExtra("bkey").toString()
        key = intent.getStringExtra("key").toString()
        gname = intent.getStringExtra("bname").toString()

        binding.titleArea.text = title
        binding.textArea.text = content
        binding.timeArea.text = time
        binding.boardArea.text = gname + " 스터디룸 자유게시판"
        getWriterData(uid)

        val myUid = FBAuth.getUid()
        val writerUid = uid

        binding.wrtImg.setOnClickListener {
            profileDialog(writerUid)
        }

        binding.nameArea.setOnClickListener {
            profileDialog(writerUid)
        }

        binding.timeArea.setOnClickListener {
            profileDialog(writerUid)
        }

        if (myUid.equals(writerUid)) {
            Log.d(TAG, "내가 쓴 글")
            binding.boardSettingIcon.isVisible = true
        }

        binding.boardSettingIcon.setOnClickListener {
            showDialog()
        }

        key = intent.getStringExtra("key").toString()
        //getBoardData(key)
        getImageData(bkey)

        binding.commentBtn.setOnClickListener {
            insertComment(bkey)
        }

        commentAdapter = CommentLVAdapter(commentDataList)
        binding.commentLV.adapter = commentAdapter

        binding.commentLV.setOnItemClickListener { parent, view, position, id ->
            profileDialog(commentDataList[position].uid)
        }

        getCommentData(bkey)

    }

    private fun getCommentData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                commentDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                }
                Log.d("dddd", commentDataList.toString())
                //adapter 동기화//
                commentAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(key).addValueEventListener(postListener)

    }

    private fun insertComment(key: String) {
        //comment
        //  - BoardKey
        //      - CommentKey
        //          - CommentData

        val rUid = UUID.randomUUID().toString()

        FBRef
            .commentRef
            .child(key)
            .push()
            .setValue(
                CommentModel(
                    binding.commentArea.text.toString(),
                    FBAuth.getTime(),
                    FirebaseAuthUtils.getUid()
                )
            )

        Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()
        binding.commentArea.setText("")
    }

    private fun showDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정/삭제")

        val alertDialog = mBuilder.show()
        alertDialog.findViewById<Button>(R.id.btnEdit11)?.setOnClickListener {

            val intent = Intent(this, GBoardEditActivity::class.java)
            intent.putExtra("key", key)
            intent.putExtra("uid", uid)
            intent.putExtra("title", title)
            intent.putExtra("content", content)
            intent.putExtra("bkey", bkey)
            intent.putExtra("time", time)
            /*

        title = intent.getStringExtra("title").toString()
        content = intent.getStringExtra("content").toString()
        time = intent.getStringExtra("time").toString()
        uid = intent.getStringExtra("uid").toString()
            */
            startActivity(intent)
            finish()
        }
        alertDialog.findViewById<Button>(R.id.btnDelete11)?.setOnClickListener {
            FBboard.insideboardRef.child(key).child(bkey).removeValue()
            Log.d("dDD", key);
            Log.d("FFF", bkey);
            Toast.makeText(this, "삭제완료", Toast.LENGTH_LONG).show()
            finish()
        }
        //mBuilder.show()
    }

    private fun profileDialog(uid: String) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.profile_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("사용자 정보")

        val alertDialog = mBuilder.show()

        //지금 유저
        val currentUser = Firebase.auth.currentUser?.uid.toString()

        var name = ""
        var grade = ""
        var info = ""
        var guid = ""

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                Log.d("abc", data.toString())

                name = data!!.nickname.toString()
                grade = data!!.gender.toString()
                info = data!!.info.toString()
                Log.d("abc", name)

                val myImage = alertDialog.findViewById<ImageView>(R.id.imageArea)

                alertDialog.findViewById<TextView>(R.id.nameArea).text = "이름 : " + name
                if (info == "" || info == "null") alertDialog.findViewById<TextView>(R.id.infoArea).text =
                    "아직 사용자가 정보를 입력하지 않았습니다."
                else alertDialog.findViewById<TextView>(R.id.infoArea).text = info

                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Glide.with(baseContext)
                            .load(task.result)
                            .into(myImage)

                    }

                })

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)

        alertDialog.findViewById<Button>(R.id.msgBtn).setOnClickListener {
            val myUserId = Firebase.auth.currentUser?.uid ?: ""
            val otherUser = uid
            val chatRoomDB = Firebase.database.reference.child(Key.DB_CHAT_ROOMS).child(myUserId)
                .child(otherUser ?: "")
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
                        otherUserName = name,
                        otherUserId = otherUser,
                    )
                    chatRoomDB.setValue(newChatRoom)
                }
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otherUser)
                intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId)
                startActivity(intent)
            }
        }

        alertDialog.findViewById<Button>(R.id.addBtn).setOnClickListener {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, dataSnapshot.toString())
                    val data = dataSnapshot.getValue(UserDataModel::class.java)

                    val dataname = data!!.nickname
                    guid = data!!.uid.toString()

                    checkUid(currentUser, guid) { result ->
                        if (result) {
                            Toast.makeText(
                                this@GBoardInsideActivity,
                                "이미 추가된 친구입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {

                            if (currentUser == guid) Toast.makeText(
                                this@GBoardInsideActivity,
                                "본인입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            else {
                                FirebaseRef.userBothRef.child(currentUser).child(guid)
                                    .setValue("true")
                                FirebaseRef.userBothRef.child(guid).child(currentUser)
                                    .setValue("true")
                                FirebaseRef.userLikeRef.child(currentUser).child(guid)
                                    .setValue("false")
                                FirebaseRef.userLikeRef.child(guid).child(currentUser)
                                    .setValue("false")
                                Toast.makeText(
                                    this@GBoardInsideActivity,
                                    dataname + "님을 친구로 바로 추가하였습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
        }
    }

    private fun checkUid(uid1: String, uid2: String, callback: (Boolean) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var check = false

                for (dataModel in dataSnapshot.children) {
                    val data = dataModel.key.toString()
                    if (data == uid2) {
                        check = true
                        break
                    }
                }
                // 데이터를 확인한 후에 콜백 호출
                callback(check)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userBothRef.child(uid1).addListenerForSingleValueEvent(postListener)
    }

    private fun getImageData(key: String) {

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.getImageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {
                //실패시 이미지 영역 없애기
                binding.getImageArea.isVisible = false
            }
        })


    }

    private fun getWriterData(uid: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                binding.nameArea.text = data!!.nickname

                val uid = data!!.uid.toString()
                val profile = binding.wrtImg

                val storageReference = Firebase.storage.reference.child(uid + ".png")
                storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {

                        if (profile != null) {
                            Glide.with(this@GBoardInsideActivity)
                                .load(task.result)
                                .into(profile)
                        }


                    } else {
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

}