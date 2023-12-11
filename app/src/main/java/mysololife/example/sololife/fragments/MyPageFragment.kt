package mysololife.example.sololife.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMainMpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.Matching
import mysololife.example.sololife.auth.LoginActivity
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef

class MyPageFragment : Fragment() {

    private val TAG = "MyPageFragment"
    private val uid = FirebaseAuthUtils.getUid()

    private lateinit var binding: ActivityMainMpBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMainMpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        binding.profileeditBtn.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_mypageFragment_to_settingFragment)
        }

        binding.likelistBtn.setOnClickListener {
            val intent = Intent(context, Matching::class.java)
            startActivity(intent)
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_LONG).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.msgBtn.setOnClickListener {
            Toast.makeText(requireContext(),"msg버튼을 클릭하였습니다.",Toast.LENGTH_SHORT).show()
        }
        binding.swiperefreshlayout.setOnRefreshListener{
            getMyData()
            binding.swiperefreshlayout.isRefreshing = false
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
                try {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                    val data = dataSnapshot.getValue(UserDataModel::class.java)

                    //myUid.text = data!!.uid
                    myNickname.text = data!!.nickname
                    myGender.text = data!!.gender
                    myInfo.text = data!!.info

                    if (myInfo.text == "") {
                        myInfo.text = "자기소개를 해주세요:)"
                    }

                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                            if (getActivity() != null) {
                                try {
                                    Glide.with(this@MyPageFragment)
                                        .load(task.result)
                                        .listener(object : RequestListener<Drawable> {
                                            override fun onLoadFailed(
                                                e: GlideException?,
                                                model: Any?,
                                                target: Target<Drawable>,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                binding.loadingProgressBar.visibility =
                                                    View.GONE // 로딩 실패 시 ProgressBar 숨김
                                                return false
                                            }

                                            override fun onResourceReady(
                                                resource: Drawable,
                                                model: Any,
                                                target: Target<Drawable>?,
                                                dataSource: DataSource,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                // target이 null이 아닌 경우에만 로딩 완료 시 ProgressBar를 숨김
                                                if (target != null) {
                                                    binding.loadingProgressBar.visibility =
                                                        View.GONE
                                                }
                                                return false
                                            }
                                        })
                                        .into(myImage)
                                } catch (e: Exception) {
                                    binding.loadingProgressBar.visibility = View.GONE
                                }

                            }

                    }
                    else{
                        binding.loadingProgressBar.visibility = View.GONE
                    }
                })
                }
                catch(e:Exception) {
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(),"데이터를 저장하는 중 오류가 발생하였습니다. 로그인 페이지로 이동합니다.",Toast.LENGTH_SHORT).show()
                    auth.signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }
}