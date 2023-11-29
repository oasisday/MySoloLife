package mysololife.example.sololife

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

import android.os.Bundle
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


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

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    // 기존에 쌓인 백 스택을 제거하고 새로운 프래그먼트로 이동합니다.
                    navHostFragment.navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.maindashboardFragment ->{
                    navHostFragment.navController.popBackStack(R.id.homeFragment, false)
                    navHostFragment.navController.navigate(R.id.maindashboardFragment)
                    true
                }
                R.id.mypageFragment ->{
                    navHostFragment.navController.popBackStack(R.id.homeFragment, false)
                    navHostFragment.navController.navigate(R.id.mypageFragment)
                    true
                }
                R.id.groupmakeFragment ->{
                    navHostFragment.navController.popBackStack(R.id.homeFragment, false)
                    navHostFragment.navController.navigate(R.id.groupmakeFragment)
                    true
                }
                // 다른 메뉴 항목들도 필요에 따라 추가합니다.
                else -> false
            }
        }
    }
}