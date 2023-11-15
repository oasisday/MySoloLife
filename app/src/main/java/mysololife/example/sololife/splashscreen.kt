package mysololife.example.sololife

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mysololife.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.auth.LoginActivity
import mysololife.example.sololife.auth.introActivity

class splashscreen : AppCompatActivity() {
    var topAnim: Animation? = null
    var bottomAnim: Animation? = null
    var topimgAnim: Animation? = null
    var imageView: ImageView? = null
    var app_name: TextView? = null
    var topimageView: ImageView? = null
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
        topimgAnim = AnimationUtils.loadAnimation(this,R.anim.png_animation)
        topimageView = findViewById(R.id.shape)
        imageView = findViewById(R.id.joinBtn)
        app_name = findViewById(R.id.app_name)


        imageView?.animation = topAnim
        app_name?.animation = bottomAnim
        topimageView?.animation = topimgAnim
        auth = Firebase.auth

        //로그아웃 상태//
        if(auth.currentUser?.uid == null){
            Toast.makeText(this,"로그아웃 상태",Toast.LENGTH_SHORT).show()
            //3초 있다가 다음 화면으로 넘어간다.
            Handler().postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 1000)

        }
        //로그인 상태//
        else{
            Toast.makeText(this,"로그인 상태",Toast.LENGTH_SHORT).show()

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