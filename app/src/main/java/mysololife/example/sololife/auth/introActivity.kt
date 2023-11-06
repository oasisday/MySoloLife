package mysololife.example.sololife.auth

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityIntroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.MainActivity


class introActivity : AppCompatActivity() {

    private lateinit var binding : ActivityIntroBinding

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        /////////////////////
        val isTiramisuOrHigher = Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU
        val notificationPermission = POST_NOTIFICATIONS

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


        /////////////////////

        binding = DataBindingUtil.setContentView(this,R.layout.activity_intro)

        //Intent를 사용하여 화면 이동//
        binding.loginBtn.setOnClickListener {
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.joinBtn.setOnClickListener {
            var intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.noAccountBtn.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this,"로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}