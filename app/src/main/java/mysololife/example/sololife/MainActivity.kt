package mysololife.example.sololife

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.translation.Translator
import androidx.databinding.DataBindingUtil.setContentView

import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.auth.introActivity
import mysololife.example.sololife.setting.SettingActivity
import mysololife.example.sololife.Matching
import mysololife.example.sololife.setting.MyPageActivity
import mysololife.example.sololife.timetable.TestTableActivity
import mysololife.example.sololife.translator.TranslateActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)

        binding.settingBtn.setOnClickListener{
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
        binding.choi.setOnClickListener {
            val intent = Intent(this, TestTableActivity::class.java)
            startActivity(intent)
        }

        binding.lee.setOnClickListener {
            val intent = Intent(this, TranslateActivity::class.java)
            startActivity(intent)
        }

    }
}