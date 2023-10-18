package mysololife.example.sololife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.mysololife.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.auth.introActivity

class SplashActivity : Activity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = Firebase.auth

        //로그아웃 상태//
        if(auth.currentUser?.uid == null){
            //3초 있다가 다음 화면으로 넘어간다.
            Handler().postDelayed({
                startActivity(Intent(this, introActivity::class.java))
                finish()
            }, 1000)

        }
        //로그인 상태//
        else{
            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1000)
        }
    
    }
}