package mysololife.example.sololife.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity


class MyPageActivity : AppCompatActivity() {
//
//    private val TAG = "MyPageActivity"
//    private val uid = FirebaseAuthUtils.getUid()
//
//    private lateinit var binding : ActivityMyPageBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_my_page)
//
//        binding = DataBindingUtil.setContentView(this,R.layout.activity_my_page)
//
//        binding.profileeditBtn.setOnClickListener{
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
//        }
//
//        binding.likelistBtn.setOnClickListener{
//            val intent = Intent(this, MyLikeListActivity::class.java)
//            startActivity(intent)
//        }
//
//        binding.msgBtn.setOnClickListener{
//            val intent = Intent(this, MyMsgActivity::class.java)
//            startActivity(intent)
//        }
//
//        getMyData()
//
//    }
//
//    private fun getMyData(){
//
////        val myUid = findViewById<TextView>(R.id.myUid)
//        val myNickname = findViewById<TextView>(R.id.myNickname)
//        val myGender = findViewById<TextView>(R.id.myGender)
//        val myImage = findViewById<ImageView>(R.id.myImage)
//        val myInfo = findViewById<TextView>(R.id.myInfo)
//
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                Log.d(TAG, dataSnapshot.toString())
//                val data = dataSnapshot.getValue(UserDataModel::class.java)
//
//                //myUid.text = data!!.uid
//                myNickname.text = data!!.nickname
//                myGender.text = data!!.gender
//                myInfo.text = data!!.info
//
//                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
//                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
//
//                    if(task.isSuccessful) {
//                        Glide.with(baseContext)
//                            .load(task.result)
//                            .into(myImage)
//
//                    }
//
//                })
//
//
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
//    }
}