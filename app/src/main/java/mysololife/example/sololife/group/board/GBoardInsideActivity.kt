package mysololife.example.sololife.group.board

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityBoardInsideBinding
import com.example.mysololife.databinding.GActivityBoardInsideBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.board.BoardModel
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

    private val TAG = GBoardInsideActivity::class.java.simpleName

    private lateinit var binding : GActivityBoardInsideBinding

    private lateinit var key:String

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter : CommentLVAdapter

    private lateinit var title:String
    private lateinit var content:String
    private lateinit var time:String
    private lateinit var uid:String
    private lateinit var bkey:String

    private val commentList = mutableListOf<CommentModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val commentLV = findViewById<ListView>(R.id.commentLV)

        binding = DataBindingUtil.setContentView(this, R.layout.g_activity_board_inside)

        title = intent.getStringExtra("title").toString()
        content = intent.getStringExtra("content").toString()
        time = intent.getStringExtra("time").toString()
        uid = intent.getStringExtra("uid").toString()
        bkey = intent.getStringExtra("bkey").toString()
        key = intent.getStringExtra("key").toString()

        binding.titleArea.text = title
        binding.textArea.text = content
        binding.timeArea.text = time
        getWriterData(uid)

        val myUid = FBAuth.getUid()
        val writerUid = uid

        if(myUid.equals(writerUid)){
            Log.d(TAG, "내가 쓴 글")
            binding.boardSettingIcon.isVisible = true
        }

        binding.boardSettingIcon.setOnClickListener{
            showDialog()
        }

        key = intent.getStringExtra("key").toString()
        //getBoardData(key)
        getImageData(bkey)

        binding.commentBtn.setOnClickListener{
            insertComment(bkey)
        }

        commentAdapter = CommentLVAdapter(commentDataList)
        binding.commentLV.adapter = commentAdapter


//        commentLV.setOnItemLongClickListener{ parent, view, position, id ->
//            Toast.makeText(this,"hi",Toast.LENGTH_SHORT).show()
//            return@setOnItemLongClickListener (false)
//        }

        getCommentData(bkey)

    }
    fun getCommentData(key : String){

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

    fun insertComment(key:String){
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

    private fun showDialog(){

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정/삭제")

        val alertDialog = mBuilder.show()
        alertDialog.findViewById<Button>(R.id.editBtn2)?.setOnClickListener{
            Toast.makeText(this, "수정버튼을 눌렀습니다.", Toast.LENGTH_LONG).show()

            val intent = Intent(this, GBoardEditActivity::class.java)
            intent.putExtra("key", key)
            intent.putExtra("uid",uid)
            intent.putExtra("title",title)
            intent.putExtra("content",content)
            intent.putExtra("bkey",bkey)
            intent.putExtra("time",time)
            /*

        title = intent.getStringExtra("title").toString()
        content = intent.getStringExtra("content").toString()
        time = intent.getStringExtra("time").toString()
        uid = intent.getStringExtra("uid").toString()
            */
            startActivity(intent)
            finish()
        }
        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener{
            FBboard.insideboardRef.child(key).child(bkey).removeValue()
            Log.d("dDD",key);
            Log.d("FFF",bkey);
            Toast.makeText(this,"삭제완료", Toast.LENGTH_LONG).show()
            finish()
        }
        //mBuilder.show()
    }
    private fun delDialog2(key: String){

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정/삭제")

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener{
            FBRef.commentRef.child(key).removeValue()
            Toast.makeText(this,"삭제완료", Toast.LENGTH_LONG).show()
            finish()
        }
        //mBuilder.show()
    }

    private fun getImageData(key : String){

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.getImageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {
                //실패시 이미지 영역 없애기
                binding.getImageArea.isVisible = false
            }
        })


    }

    private fun getWriterData(uid:String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                binding.nameArea.text = "작성자: " + data!!.nickname



//                myUid.text = data!!.uid
//                myNickname.text = data!!.nickname
//                myGender.text = data!!.gender
//
//                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
//                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        if(getActivity() !=null){
//                            Glide.with(this@MyPageFragment)
//                                .load(task.result)
//                                .into(myImage)}
//                    }
//                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

    private fun getBoardData(key : String){



//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                try {
//                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
//                    Log.d(TAG, dataModel!!.title)
//
//                    binding.titleArea.text = dataModel!!.title
//                    binding.textArea.text = dataModel!!.content
//                    binding.timeArea.text = dataModel!!.time
//
//                    val myUid = FBAuth.getUid()
//                    val writerUid = dataModel.uid
//
//                    if(myUid.equals(writerUid)){
//                        Log.d(TAG, "내가 쓴 글")
//                        binding.boardSettingIcon.isVisible = true
//                    }
//                }
//                catch (e:Exception){
//                    Log.d(TAG, "삭제완료")
//                }
//
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FBRef.boardRef.child(key).addValueEventListener(postListener)
    }

}