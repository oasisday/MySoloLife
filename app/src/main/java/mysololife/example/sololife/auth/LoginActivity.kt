package mysololife.example.sololife.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.MainActivity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Patterns
import com.example.mysololife.databinding.ActivityLoginFinalBinding
import java.util.regex.Pattern

class LoginActivity : Activity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding : ActivityLoginFinalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        binding = ActivityLoginFinalBinding.inflate(layoutInflater).apply{
            setContentView(root)
        }
        binding.loginBtn.setOnClickListener {

            val email = binding.emailArea.text.toString()
            val password = binding.passwordArea.text.toString()

            val pattern: Pattern = Patterns.EMAIL_ADDRESS

            var isGoToJoin = true

            if(email.isEmpty()){
                Toast.makeText(this,"이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if(password.isEmpty()){
                Toast.makeText(this,"비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if(!pattern.matcher(email.toString()).matches()){
                Toast.makeText(this, "이메일 형식을 확인하세요.", Toast.LENGTH_SHORT).show()
            }

            if(isGoToJoin == true) {
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
                                Toast.makeText(this, "아이디 혹은 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
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