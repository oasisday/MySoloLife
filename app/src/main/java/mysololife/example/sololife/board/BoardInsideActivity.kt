package mysololife.example.sololife.board

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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.auth.UserInfoModel
import mysololife.example.sololife.comment.CommentLVAdapter
import mysololife.example.sololife.comment.CommentModel
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBRef
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import mysololife.example.sololife.utils.FirebaseRef.Companion.userLikeRef
import java.sql.Types.NULL
import java.util.UUID

class BoardInsideActivity : Activity() {

    private val TAG = BoardInsideActivity::class.java.simpleName

    private lateinit var binding: ActivityBoardInsideBinding

    private lateinit var key: String
    private val commentDataList = mutableListOf<CommentModel>()
    private lateinit var commentAdapter: CommentLVAdapter
    private lateinit var Uid: String
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        binding.boardSettingIcon.setOnClickListener {
            showDialog()
        }

        key = intent.getStringExtra("key").toString()
        Uid = FirebaseAuthUtils.getUid()
        getBoardData(key)
        getImageData(key)

        //////////////

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                binding.nameArea.text = data!!.nickname

                val uid = data!!.uid.toString()
                val profile = binding.wrtImg


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
        //////////////

        binding.commentBtn.setOnClickListener {
            if (binding.commentArea.text.toString() == "") {
                Toast.makeText(this, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                insertComment(key)
            }
        }

        commentAdapter = CommentLVAdapter(commentDataList)
        binding.commentLV.adapter = commentAdapter

        getCommentData(key)
        //getWriterData(uid)

        binding.commentLV.setOnItemClickListener { parent, view, position, id ->
            profileDialog(commentDataList[position].uid)
        }


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
                    if(task.isSuccessful) {

                        if (profile != null) {
                            Glide.with(this@BoardInsideActivity)
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

    fun getCommentData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                commentDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                }

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

    fun insertComment(key: String) {
        //comment
        //  - BoardKey
        //      - CommentKey
        //          - CommentData


        val rkey = UUID.randomUUID().toString()

// Create a reference to the "comments" node under your "commentRef"
        val commentsRef = FBRef.commentRef.child(key)

// Get a new push key
        val commentKey = commentsRef.push().key

// Create a CommentModel object
        val commentModel = CommentModel(
            binding.commentArea.text.toString(),
            FBAuth.getTime(),
            FirebaseAuthUtils.getUid(),
            rkey
        )

// Set the comment data at the specific UID
        commentKey?.let {
            val specificUid = rkey // Replace with the desired UID value
            commentsRef.child(specificUid).setValue(commentModel)
                .addOnSuccessListener {
                    // Handle success
                    println("Comment added successfully!")
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    println("Error adding comment: $e")
                }
        }

        Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()
        binding.commentArea.setText("")
    }

    private fun showDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정/삭제")

        val alertDialog = mBuilder.show()
        alertDialog.findViewById<Button>(R.id.btnEdit)?.setOnClickListener {

            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            finish()
        }
        alertDialog.findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
            FBRef.boardRef.child(key).removeValue()
            Toast.makeText(this, "삭제완료", Toast.LENGTH_LONG).show()
            finish()
        }
        //mBuilder.show()
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

    private fun getBoardData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
                    Log.d(TAG, dataModel!!.title)

                    binding.titleArea.text = dataModel!!.title
                    binding.textArea.text = dataModel!!.content
                    binding.timeArea.text = dataModel!!.time

                    val myUid = FBAuth.getUid()
                    val writerUid = dataModel.uid
                    uid = dataModel!!.uid
                    getWriterData(uid)
                    if (myUid.equals(writerUid)) {
                        Log.d(TAG, "내가 쓴 글")
                        binding.boardSettingIcon.isVisible = true
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "삭제완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.child(key).addValueEventListener(postListener)
    }

    private fun delDialog2(key: String) {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정/삭제")

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.btnDelete)?.setOnClickListener {
            FBRef.commentRef.child(key).removeValue()
            Toast.makeText(this, "삭제완료", Toast.LENGTH_LONG).show()
            finish()
        }
        alertDialog.findViewById<Button>(R.id.btnDelete)?.setOnClickListener {
            FBRef.commentRef.child(key).removeValue()
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
                alertDialog.findViewById<TextView>(R.id.gradeArea).text = "학년 : " + grade
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
                                this@BoardInsideActivity,
                                "이미 추가된 친구입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {

                            if(currentUser == guid) Toast.makeText(this@BoardInsideActivity,"본인입니다.",Toast.LENGTH_SHORT).show()
                            else{
                                userLikeRef.child(currentUser).child(guid).setValue("true")
                                userLikeOtherUser(currentUser, guid)
                                Toast.makeText(
                                    this@BoardInsideActivity,
                                    dataname + "님을 친구로 추가하였습니다.",
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

    private fun userLikeOtherUser(myUid: String, otherUid: String) {
        FirebaseRef.userLikeRef.child(uid).child(otherUid).setValue("true")

        getOtherUserLikeList(otherUid)
    }

    private fun getOtherUserLikeList(otherUid: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
                    val likeUserKey = dataModel.key.toString()
                    if (likeUserKey.equals(uid)) {
                        Toast.makeText(
                            this@BoardInsideActivity,
                            "matching success!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        createNotificationChannel()
                        sendNotification()
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_ch", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        var builder = NotificationCompat.Builder(this, "Test_ch")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Study Matching")
            .setContentText("새로운 스터디원과 연결되었습니다. 확인해보세요!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@BoardInsideActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(123, builder.build())
        }
    }

    //Uid1에 uid2가 있으면 true
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

        userLikeRef.child(uid1).addListenerForSingleValueEvent(postListener)
    }
}