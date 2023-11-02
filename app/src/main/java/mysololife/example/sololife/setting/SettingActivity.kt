package mysololife.example.sololife.setting

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivitySettingBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.introActivity
import mysololife.example.sololife.utils.FBAuth
import java.io.ByteArrayOutputStream

class SettingActivity : Activity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding : ActivitySettingBinding
    private var isImageUpload = false                           //프로필 사진 업로드 되어있는지

    private val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this,R.layout.activity_setting)
        //val logoutBtn = findViewById<Button>(R.id.LogoutBtn)

        binding.LogoutBtn.setOnClickListener{
            auth.signOut()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_LONG).show()

            val intent = Intent(this, introActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.profileArea.setOnClickListener{

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, IMAGE_PICK_CODE)
            isImageUpload = true

        }

        binding.SettingBtn.setOnClickListener{

            val ref = FirebaseStorage.getInstance().getReference()

            val bitmap = (binding.profileArea.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val mountainRef = ref.child(FBAuth.getUid() + "_profile")

            val uploadTask = mountainRef.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(this,"실패",Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
            }
        }

        binding.SettingBtn.setOnClickListener{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // 외부 저장소에 쓰기 권한이 있는지 확인
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 부여되지 않은 경우 권한 요청
                    ActivityCompat.requestPermissions(this, arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_PERMISSION_REQUEST)
                } else {
                    // 권한이 이미 부여된 경우 비트맵으로 저장

                }
            }
            else{
                pickImageFromGallery();
            }
        }

        FirebaseStorage.getInstance().getReference().child(FBAuth.getUid()+"_profile").downloadUrl
            .addOnCompleteListener(OnCompleteListener<Uri>{task->
                if(task.isSuccessful){
                    Glide.with(this)
                        .load(task.result)
                        .into(binding.profileArea)
                }else{
                    Toast.makeText(this, task.exception!!.message,Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun imageUpload(key : String){

        // Get the data from an ImageView as bytes
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child(key + ".png")

        val imageView = binding.profileArea
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

    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }
    companion object{
        private val IMAGE_PICK_CODE = 1000;
        private val PERMISSION_CODE = 1001;
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery()
                }
                else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            binding.profileArea.setImageURI(data?.data)
        }
    }

}