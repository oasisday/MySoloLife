package mysololife.example.sololife.auth

import android.Manifest
import android.R.attr.src
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mysololife.databinding.ActivityLoginFinalBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import mysololife.example.sololife.MainActivity
import mysololife.example.sololife.utils.FirebaseRef
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {
    private lateinit var emailLoginResult: ActivityResultLauncher<Intent>
    private lateinit var pendingUser: User
    private val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            showErrorToast(error.toString())
            error.printStackTrace()
        } else if (token != null) {
            getKakaoAccountInfo()
        }
    }
    private lateinit var auth: FirebaseAuth
    val TAG = "KakaoLogin"
    private lateinit var binding: ActivityLoginFinalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        auth = Firebase.auth

        //알림 권한 설정

        val isTiramisuOrHigher = Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU
        val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

        var hasNotificationPermission =
            if (isTiramisuOrHigher)
                ContextCompat.checkSelfPermission(this, notificationPermission) == PackageManager.PERMISSION_GRANTED
            else true
        val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            hasNotificationPermission = it

        }

        if (!hasNotificationPermission) {
            launcher.launch(notificationPermission)
        }

        binding = ActivityLoginFinalBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }


        //카카오 sdk 초기화 해주기
        KakaoSdk.init(this, "903acc73748f829977599eb159665724")

        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error == null) {
                    getKakaoAccountInfo()
                }
            }
        }


        //회원가입 구현
        binding.signupBtn.setOnClickListener {
            var intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        //카카오 로그인 구현
        binding.kakaoBtn.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                //카카오톡 로그인
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        //카카오톡 로그인 실패
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }
                        //카카오톡계정 로그인
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        if (Firebase.auth.currentUser == null) {
                            //카카오톡에서 정보 가져와서 파이어베이스 로그인
                            getKakaoAccountInfo()
                        } else {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
            } else {
                //카카오계정 로그인
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        binding.LoginBtn.setOnClickListener {
            val email = binding.emailArea.text.toString()
            val password = binding.pwArea.text.toString()
            val pattern: Pattern = Patterns.EMAIL_ADDRESS
            var isGoToJoin = true
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if (!pattern.matcher(email.toString()).matches()) {
                Toast.makeText(this, "이메일 형식을 확인하세요.", Toast.LENGTH_SHORT).show()
            }

            if (isGoToJoin == true) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                            Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            if (!NetworkManager.checkNetworkState(this)) {
                                Toast.makeText(this, "네트워크 연결상태가 좋지 않습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(this, "아이디 혹은 비밀번호를 확인하세요.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
            }
        }
    }

    private fun showErrorToast(error: String) {
        Toast.makeText(this, "사용자 로그인에 실패했습니다." + "오류 : " + error, Toast.LENGTH_SHORT).show()
    }

    private fun getKakaoAccountInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                showErrorToast(error.toString())
                error.printStackTrace()
                Log.d("testing", error.toString())
            } else if (user != null) {
                // 사용자 정보 요청 성공
                signInFirebase(user)
            }
        }
    }

    private fun signInFirebase(user: User) {
        val uId = user.id.toString()
        val email = user.id.toString() + "@kakao.com"
        Firebase.auth.createUserWithEmailAndPassword(email, uId).addOnCompleteListener {
            if (it.isSuccessful) {
                updateFirebaseDatabase(user)
            }
        }.addOnFailureListener {
            // 이미 가입된 계정
            if (it is FirebaseAuthUserCollisionException) {
                Firebase.auth.signInWithEmailAndPassword(email, uId)
                    .addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            updateFirebaseDatabase(user)
                        } else {
                            showErrorToast(" 아이디 충돌 오류 발생")
                        }
                    }.addOnFailureListener { error ->
                        error.printStackTrace()
                        showErrorToast(error.toString())
                    }
            } else {
                showErrorToast(" 파이어 베이스 관련 오류 발생")
            }
        }
    }

    private fun updateFirebaseDatabase(user: User) {
        val uid = Firebase.auth.currentUser?.uid.orEmpty()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fatching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result

                Log.e(TAG, token.toString())

                val userModel = UserDataModel(
                    uid,
                    user.kakaoAccount?.name.orEmpty(),
                    user.kakaoAccount?.gender.toString(),
                    token
                )

                FirebaseRef.userInfoRef.child(uid).setValue(userModel)
                uploadImage(uid,user.kakaoAccount?.profile?.thumbnailImageUrl.orEmpty())

                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)

                //기존 액티비티를 다 날려버린다//
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            })
    }

    private fun uploadImage(uid: String, link: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")
        try {
            val url = URL(link)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val bitmap =  BitmapFactory.decodeStream(input)
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
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

public class NetworkManager {

    companion object {
        fun checkNetworkState(context: Context): Boolean {
            val connectivityManager: ConnectivityManager =
                context.getSystemService(ConnectivityManager::class.java)
            val network: Network = connectivityManager.activeNetwork ?: return false
            val actNetwork: NetworkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                else -> false
            }
        }
    }
}