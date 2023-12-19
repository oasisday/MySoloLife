package mysololife.example.sololife.board

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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBRef
import java.io.ByteArrayOutputStream

class BoardWriteActivity : Activity() {

    private lateinit var binding : ActivityBoardWriteBinding
    private val TAG = BoardWriteActivity::class.java.simpleName
    private var isImageUpload = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var checked = true
        binding = ActivityBoardWriteBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.writeBtn.setOnClickListener{

            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            if (title == ""){
                Toast.makeText(this,"제목을 확인해주세요.", Toast.LENGTH_SHORT).show()
                checked = false
            }
            else checked = true


            //파이어베이스 스토리지 이미지 저장을 하고 싶음
            //게시글 클릭-> 정보를 받아 와야함

            if(checked) {
                val key = FBRef.boardRef.push().key.toString()

                FBRef.boardRef
                    .child(key)             //랜덤 생성//
                    .setValue(BoardModel(title, content, uid, time))

                Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_LONG).show()

                if (isImageUpload == true) {
                    Toast.makeText(this, "이미지가 업로드 되는데 약간의 시간이 소요됩니다.", Toast.LENGTH_SHORT).show()
                    imageUpload(key)
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

    private fun imageUpload(key : String){

        // Get the data from an ImageView as bytes
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child(key + ".png")

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
}