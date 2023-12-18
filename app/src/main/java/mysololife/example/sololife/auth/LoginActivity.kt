package mysololife.example.sololife.auth

import android.Manifest
import android.R.attr.src
import android.app.Activity
import android.app.ProgressDialog
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.mysololife.databinding.ActivityLoginFinalBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
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

    private val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            showErrorToast(error.toString())
            error.printStackTrace()
            hideProgressDialog()
        } else if (token != null) {
            getKakaoAccountInfo()
        }
    }
    private lateinit var auth: FirebaseAuth
    val TAG = "KakaoLogin"
    private lateinit var binding: ActivityLoginFinalBinding
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var keyHash = Utility.getKeyHash(this)
        Log.e(TAG, "해시 키 값 : ${keyHash}")

        auth = Firebase.auth
        //알림 권한 설정
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("로그인 중...")
        progressDialog.setCancelable(false)

        askNotificationPermission()

        binding = ActivityLoginFinalBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        //카카오 sdk 초기화 해주기
        KakaoSdk.init(this, "903acc73748f829977599eb159665724")
//        if (AuthApiClient.instance.hasToken()) {
//            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
//                if (error == null) {
//                    getKakaoAccountInfo()
//                }
//            }
//        }

        //회원가입 구현
        binding.signupBtn.setOnClickListener {
            var intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        //카카오 로그인 구현
        binding.kakaoBtn.setOnClickListener {
            showProgressDialog()
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                //카카오톡 로그인
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        //카카오톡 로그인 실패
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            hideProgressDialog()
                            return@loginWithKakaoTalk
                        }
                        //카카오톡계정 로그인
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        if (Firebase.auth.currentUser == null) {
                            //카카오톡에서 정보 가져와서 파이어베이스 로그인
                            getKakaoAccountInfo()
                        } else {
                            hideProgressDialog()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    else{
                        hideProgressDialog()
                        Toast.makeText(this, "카카오톡 로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
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
                showProgressDialog()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            hideProgressDialog()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            hideProgressDialog()
                            if (!NetworkManager.checkNetworkState(this)) {
                                Toast.makeText(this, "네트워크 연결상태가 좋지 않습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(this, "아이디 혹은 비밀번호를 확인하세요.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        hideProgressDialog()
                        Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // 알림권한 없음
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationalDialog()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("알림 권한이 없으면 알림을 받을 수 없습니다.")
            .setPositiveButton("권한 허용하기") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }


    private fun showErrorToast(error: String) {
        Toast.makeText(this, "사용자 로그인에 실패했습니다." + "오류 : " + error, Toast.LENGTH_SHORT).show()
    }

    private fun getKakaoAccountInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                showErrorToast(error.toString())
                error.printStackTrace()
                hideProgressDialog()

            } else if (user != null) {
                // 사용자 정보 요청 성공
                signInFirebase(user)
            }
            else{
                hideProgressDialog()
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
                            hideProgressDialog()
                            showErrorToast(" 아이디 충돌 오류 발생")
                        }
                    }.addOnFailureListener { error ->
                        error.printStackTrace()
                        showErrorToast(error.toString())
                        hideProgressDialog()
                    }
            } else {
                hideProgressDialog()
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
                    hideProgressDialog()
                    return@OnCompleteListener
                }


                val token = task.result
                val userModel = UserDataModel(
                    uid = uid,
                    nickname = user.kakaoAccount?.profile?.nickname.toString(),
                    "성별 선택",
                    token = token
                )
                FirebaseRef.userInfoRef.child(uid).setValue(userModel)

                //내가 만든거
                val kakaouser = mutableMapOf<String,Any>()
                kakaouser["userId"] = uid
                kakaouser["username"] = user.kakaoAccount?.profile?.nickname.toString()
                kakaouser["fcmToken"] = token
                //채팅방 기능
                Firebase.database.reference.child(Key.DB_USERS).child(uid).updateChildren(kakaouser)

                //위치 공유 관련 파이어베이스
                val personMap = mutableMapOf<String, Any>()
                personMap["uid"] = uid
                personMap["name"] = user.kakaoAccount?.profile?.nickname.orEmpty()
                personMap["profilePhoto"] = user.kakaoAccount?.profile?.thumbnailImageUrl.orEmpty()

                Firebase.database.reference.child("Person").child(uid).updateChildren(personMap)

                uploadImage(uid, user.kakaoAccount?.profile?.thumbnailImageUrl.orEmpty())
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
                val intent = Intent(this, MainActivity::class.java)
                //기존 액티비티를 다 날려버린다//
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            })
    }

    private fun uploadImage(uid: String, link: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")
        Thread {
            try {
                val url = URL(link)
                val connection =
                    url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                var uploadTask = storageRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    // Handle unsuccessful uploads
                }.addOnSuccessListener { taskSnapshot ->

                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }.start()
    }
    private fun showProgressDialog() {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    private fun hideProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
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