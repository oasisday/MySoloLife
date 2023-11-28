package mysololife.example.sololife.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMyPageBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.Matching
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.message.MyLikeListActivity
import mysololife.example.sololife.message.MyMsgActivity
import mysololife.example.sololife.setting.SettingActivity
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef

class MyPageFragment : Fragment() {

    private val TAG = "MyPageFragment"
    private val uid = FirebaseAuthUtils.getUid()

    private lateinit var binding: ActivityMyPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileeditBtn.setOnClickListener {
            val intent = Intent(activity, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.likelistBtn.setOnClickListener {
            val intent = Intent(context, Matching::class.java)
            startActivity(intent)
        }

        binding.msgBtn.setOnClickListener {
            val intent = Intent(activity, MyMsgActivity::class.java)
            startActivity(intent)
        }

        getMyData()
    }

    private fun getMyData() {
        //val myUid = binding.myUid
        val myNickname = binding.myNickname
        val myGender = binding.myGender
        val myImage = binding.myImage
        val myInfo = binding.myInfo

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                //myUid.text = data!!.uid
                myNickname.text = data!!.nickname
                myGender.text = data!!.gender
                myInfo.text = data!!.info

                if(myInfo.text == ""){
                    myInfo.text = "자기소개를 해주세요:)"
                }

                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if(getActivity() !=null){
                        Glide.with(this@MyPageFragment)
                            .load(task.result)
                            .into(myImage)}
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }
}