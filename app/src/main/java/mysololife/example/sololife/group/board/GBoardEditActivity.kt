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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityBoardEditBinding
import com.example.mysololife.databinding.ActivityBoardWriteBinding
import com.example.mysololife.databinding.GActivityBoardEditBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBRef
import mysololife.example.sololife.utils.FBboard
import java.io.ByteArrayOutputStream

class GBoardEditActivity : Activity() {

    private lateinit var key:String

    private lateinit var binding : GActivityBoardEditBinding

    private var TAG = GBoardEditActivity::class.java.simpleName

    private lateinit var writerUid : String

    private var isImageUpload2 = false

    private lateinit var title:String
    private lateinit var content:String
    private lateinit var time:String
    private lateinit var uid:String
    private lateinit var bkey:String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.g_activity_board_edit)

        key = intent.getStringExtra("key").toString()

        title = intent.getStringExtra("title").toString()
        content = intent.getStringExtra("content").toString()
        time = intent.getStringExtra("time").toString()
        uid = intent.getStringExtra("uid").toString()
        bkey = intent.getStringExtra("bkey").toString()


        binding.titleArea.setText(title)
        binding.contentArea.setText(content)


        //getBoardData(bkey)
        getImageData(bkey)

        binding.editBtn.setOnClickListener{
            editBoardData(key)
        }

        binding.imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload2 = true
        }

    }

    private fun editBoardData(key : String){

        FBRef.boardRef
            .child(key)
            .setValue(
                BoardModel(binding.titleArea.text.toString(),
                    binding.contentArea.text.toString(),
                    uid,
                    FBAuth.getTime())
            )

        Toast.makeText(this, "수정완료", Toast.LENGTH_LONG).show()

        if(isImageUpload2 == true){
            Toast.makeText(this,"이미지업로드",Toast.LENGTH_LONG).show()
            imageUpload()
        }

        finish()

    }

    private fun getImageData(key : String){

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.imageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {

            }
        })


    }


    private fun getBoardData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                Log.d("mmmm",dataModel?.title.toString())

                binding.titleArea.setText(dataModel?.title)
                binding.contentArea.setText((dataModel?.content))
                //writerUid = dataModel!!.uid.toString()

            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.insideboardRef.child(key).addValueEventListener(postListener)
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

}