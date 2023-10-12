package mysololife.example.sololife.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.MainActivity

class  JoinActivity : Activity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding : ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        binding = DataBindingUtil.setContentView(this,R.layout.activity_join)

        binding.joinBtn.setOnClickListener {

            var isGoToJoin = true

            val email = binding.emailArea.text.toString()
            val password1 = binding.pwArea.text.toString()
            val password2 = binding.pwArea2.text.toString()

            if(email.isEmpty()){
                Toast.makeText(this,"이메일을 입력해주세요", Toast.LENGTH_LONG)
                isGoToJoin = false
            }

            if(password1.isEmpty()){
                Toast.makeText(this,"비밀번호를 입력해주세요", Toast.LENGTH_LONG)
                isGoToJoin = false
            }

            if(password2.isEmpty()){
                Toast.makeText(this,"비밀번호를 다시 입력해주세요", Toast.LENGTH_LONG)
                isGoToJoin = false
            }

            //비밀번호가 같은지 확인//
            if(!password1.equals(password2)){
                Toast.makeText(this, "비밀번호를 똑같이 확인해주세요", Toast.LENGTH_LONG)
                isGoToJoin = false
            }

            if(password1.length < 6){
                Toast.makeText(this, "비밀번호를 6자리 이상으로 입력해주세요", Toast.LENGTH_LONG)
                isGoToJoin = false
            }

            //비밀번호가 모두 정확하면 실행//
            if(isGoToJoin){
                auth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(this) { task ->
                        //성공했을때//
                        if (task.isSuccessful) {

                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)

                            //기존 액티비티를 다 날려버린다//
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        //실패했을때//
                        else {
                            Toast.makeText(this, "로그인 실패!", Toast.LENGTH_LONG).show()
                        }
                    }



                }
        }


    }
}

