package mysololife.example.sololife

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mysololife.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.auth.introActivity

class splashscreen : AppCompatActivity() {
    var topAnim: Animation? = null
    var bottomAnim: Animation? = null
    var imageView: ImageView? = null
    var app_name: TextView? = null

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        imageView = findViewById(R.id.imageView2)
        app_name = findViewById(R.id.app_name)
        imageView?.animation = topAnim
        app_name?.animation = bottomAnim

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

    companion object {
        private const val DELAY_TIME = 4000
    }
}