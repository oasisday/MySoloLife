package mysololife.example.sololife.group.board

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityBoardWriteBinding
import com.example.mysololife.databinding.GActivityBoardWriteBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.board.BoardWriteActivity
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBRef
import mysololife.example.sololife.utils.FBboard
import java.io.ByteArrayOutputStream
import java.util.UUID

class GBoardWriteActivity : Activity() {

    private lateinit var binding : ActivityBoardWriteBinding
    private val TAG = BoardWriteActivity::class.java.simpleName
    private var isImageUpload = false

    // 받아온 key값
    private lateinit var key:String

    //board key값
    private val randomUUID: UUID = UUID.randomUUID()
    private val bkey: String = randomUUID.toString()

    private lateinit var groupkey:String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityBoardWriteBinding.inflate(layoutInflater).apply{
            setContentView(root)
        }
        key = intent.getStringExtra("key").toString()

        getGroupData(key)

        var checked = true

        binding.writeBtn.setOnClickListener {

            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()
            val board_key = bkey

            Log.d(TAG, title)
            Log.d(TAG, content)

            //파이어베이스 스토리지 이미지 저장을 하고 싶음
            //게시글 클릭-> 정보를 받아 와야함


            if (title == "") {
                Toast.makeText(this, "제목을 확인해주세요.", Toast.LENGTH_SHORT).show()
                checked = false
            }else{
                checked = true
            }

            //파이어베이스 스토리지 이미지 저장을 하고 싶음
            //게시글 클릭-> 정보를 받아 와야함

            if (checked) {

                FBboard.insideboardRef
                    .child(key)
                    .child(bkey)
                    .setValue(BoardModel(title, content, uid, time, board_key))

                Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_LONG).show()

                if (isImageUpload == true) {
                    Toast.makeText(this, "이미지가 업로드 되는데 약간의 시간이 소요됩니다.", Toast.LENGTH_SHORT).show()
                    imageUpload()
                }

                finish()
            }
        }
        binding.imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true
        }

    }

    private fun imageUpload(){

        // Get the data from an ImageView as bytes
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child(bkey + ".png")

        val imageView = binding.imageArea
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == 100){
            binding.imageArea.setImageURI(data?.data)
        }
    }

    private fun getGroupData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)
                    groupkey = dataModel!!.groupnum.toString()

                }
                catch (e:Exception){
                    //Log.d(TAG, "삭제완료")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)
    }

}