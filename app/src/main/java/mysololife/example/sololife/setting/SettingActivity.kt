package mysololife.example.sololife.setting

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivitySettingBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.auth.LoginActivity
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseRef
import java.io.ByteArrayOutputStream

class SettingActivity : AppCompatActivity() {

    private val TAG = SettingActivity::class.java.simpleName

    private lateinit var auth : FirebaseAuth
    private lateinit var binding : ActivitySettingBinding

    private var nickname = ""
    private var gender = ""
    private var uid = ""
    private var info = ""

    //이미 쓴 게 있으면//
    private lateinit var writerUid : String
    private lateinit var key:String

    lateinit var profileImage : ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this,R.layout.activity_setting)

        val user = auth.currentUser
        key = user?.uid.toString()
        getBoardData(key)
        getImageData(key)

        profileImage = findViewById(R.id.profileImage)
        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                profileImage.setImageURI(uri)
            }
        )

        profileImage.setOnClickListener{
            getAction.launch("image/*")
        }

        binding.LogoutBtn.setOnClickListener{
            auth.signOut()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_LONG).show()

            val intent = Intent(this, LoginActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }


        binding.SettingBtn.setOnClickListener{
           try{
            gender = findViewById<TextInputEditText>(R.id.genderArea).text.toString()
            nickname = findViewById<TextInputEditText>(R.id.nicknameArea).text.toString()
            info = findViewById<TextInputEditText>(R.id.infoArea).text.toString()

            val currentUserId = Firebase.auth.currentUser?.uid ?:""
            val currentUserDB = Firebase.database.reference.child(Key.DB_USERS).child(currentUserId)


            val userModel = UserDataModel(
                uid = currentUserId,
                nickname = nickname,
                gender =gender,
                info = info
            )

            val user = mutableMapOf<String,Any>()
            user["username"] = nickname
            user["descriotion"] = info
            currentUserDB.updateChildren(user)

            FirebaseRef.userInfoRef.child(currentUserId).setValue(userModel)

            uploadImage(currentUserId)

            Toast.makeText(this,"사용자 정보 설정완료",Toast.LENGTH_SHORT).show()
           finish()
           }

           catch (e:Exception){
               Log.d("whatsproblem",e.toString())
           }
        }


    }

    private fun uploadImage(uid : String){

        val storage = Firebase.storage
        val storageRef = storage.reference.child("$uid.png")

        // Get the data from an ImageView as bytes
        profileImage.isDrawingCacheEnabled = true
        profileImage.buildDrawingCache()
        val bitmap = (profileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }


    }

    private fun getImageData(uid : String){

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child("$uid.png")

        // ImageView in your Activity
        val imageViewFromFB = binding.profileImage

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {

            }
        })
    }

    private fun getBoardData(uid : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val dataModel = dataSnapshot.getValue(UserDataModel::class.java)

                binding.nicknameArea.setText(dataModel?.nickname)
                binding.genderArea.setText((dataModel?.gender))
                binding.infoArea.setText((dataModel?.info))
                writerUid = dataModel!!.uid.toString()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }
}