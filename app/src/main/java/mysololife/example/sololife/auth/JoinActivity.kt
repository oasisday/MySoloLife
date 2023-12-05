package mysololife.example.sololife.auth

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.MainActivity
import java.util.regex.Pattern
import androidx.core.app.ActivityCompat
import com.example.mysololife.databinding.ActivitySignupFinalBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.ktx.database
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.Key.Companion.DB_USERS
import mysololife.example.sololife.utils.FirebaseRef
import java.io.ByteArrayOutputStream

class JoinActivity : AppCompatActivity() {
    private val TAG = "JoinActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignupFinalBinding
    private var uid = ""
    lateinit var profileImage: ImageView
    var imgcheck = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        //로딩화면

        binding = ActivitySignupFinalBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        profileImage = findViewById(R.id.profileImage)
        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                profileImage.setImageURI(uri)
            }
        )

        profileImage.setOnClickListener {
            getAction.launch("image/*")
            imgcheck = true
        }

        binding.joinBtn.setOnClickListener {

            var isGoToJoin = true

            val email = binding.emailArea.text.toString()
            val password1 = binding.pwArea.text.toString()
            val password2 = binding.pwArea2.text.toString()

            val name = binding.nameArea.text.toString()
            val gender = binding.genderArea.text.toString()

            val pattern: Pattern = Patterns.EMAIL_ADDRESS

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if (password1.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if (!pattern.matcher(email.toString()).matches()) {
                Toast.makeText(this, "이메일 형식을 확인하세요.", Toast.LENGTH_SHORT).show()
            }
            if (password2.isEmpty()) {
                Toast.makeText(this, "비밀번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            //비밀번호가 같은지 확인//
            if (!password1.equals(password2)) {
                Toast.makeText(this, "비밀번호를 똑같이 확인해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if (password1.length < 6) {
                Toast.makeText(this, "비밀번호를 6자리 이상으로 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }


            //비밀번호가 모두 정확하면 실행//
            if (isGoToJoin) {
                loginUser(email, password1, name, gender)
            }
        }

    }

    private fun loginUser(
        email: String,
        password1: String,
        name: String,
        gender: String
    ) {
        auth.createUserWithEmailAndPassword(email, password1)
            .addOnCompleteListener(this) { task ->
                //성공했을때//
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    uid = user?.uid.toString()

                    FirebaseMessaging.getInstance().token.addOnCompleteListener(
                        OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(
                                    TAG,
                                    "Fatching FCM registration token failed",
                                    task.exception
                                )
                                return@OnCompleteListener
                            }

                            val token = task.result

                            Log.e(TAG, token.toString())

                            val userModel = UserDataModel(
                                uid,
                                name,
                                gender,
                                null,
                                token,
                                false
                            )

                            val userInfo = UserInfoModel(
                                uid,
                                name,
                                email
                            )

                            val user = mutableMapOf<String, Any>()
                            user["userId"] = uid
                            user["username"] = name
                            user["fcmToken"] = token
                            //채팅방 기능
                            Firebase.database.reference.child(DB_USERS).child(uid)
                                .updateChildren(user)

                            FirebaseRef.userInfoRef.child(uid).setValue(userModel)
                            FirebaseRef.userDataRef.child(uid).setValue(userInfo)
                            if (imgcheck) uploadImage(uid)
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            //기존 액티비티를 다 날려버린다//
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        })
                }
                //실패했을때//
                else {
                    if (!NetworkManager.checkNetworkState(this)) {
                        Toast.makeText(this, "네트워크 연결상태가 좋지 않습니다.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "?회원가입 오류.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    private fun uploadImage(uid: String) {

        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")


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
}

