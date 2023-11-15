package mysololife.example.sololife.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentHomeBinding
import com.example.mysololife.databinding.FragmentTimetableBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.CameraActivity
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.recorder.RecorderMainActivity
import mysololife.example.sololife.timetable.TimeTableActivity
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment(){
    lateinit var binding: FragmentHomeBinding
    private val uid = FirebaseAuthUtils.getUid()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVoiceRecorder.setOnClickListener {
            Intent(getActivity(),RecorderMainActivity::class.java).apply{
                startActivity(this)
            }
        }

        binding.btnTimeTable.setOnClickListener {
            Intent(getActivity(), TimeTableActivity::class.java).apply{
                startActivity(this)
            }
        }

        binding.btnCamera.setOnClickListener {
            Intent(getActivity(), CameraActivity::class.java).apply{
                startActivity(this)
            }
        }
        getMyData()
    }

    private fun getMyData() {
        val myImage = binding.profileImage

        val myNickname = binding.nicknameTextView

        val requestOptions = RequestOptions().circleCrop()
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)
                myNickname.text = data!!.nickname+" 님"
                val storageRef = Firebase.storage.reference.child(data!!.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if(getActivity() !=null){
                            Glide.with(this@HomeFragment)
                                .load(task.result)
                                .apply(requestOptions)
                                .into(myImage)}
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

}