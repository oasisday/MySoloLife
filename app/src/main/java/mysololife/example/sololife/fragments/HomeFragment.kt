package mysololife.example.sololife.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager

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
import mysololife.example.sololife.Matching
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.group.GroupMainActivity
import mysololife.example.sololife.group.GroupboardLVAdapter
import mysololife.example.sololife.recorder.RecorderMainActivity
import mysololife.example.sololife.timetable.TimeTableActivity
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment(){
    lateinit var binding: FragmentHomeBinding
    private val uid = FirebaseAuthUtils.getUid()
    private val boardDataList = mutableListOf<GroupDataModel>()
    private val boardKeyList = mutableListOf<String>()
    private lateinit var gboardRVAdapter: GroupboardLVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        gboardRVAdapter = GroupboardLVAdapter(boardDataList)

        binding.gotoBoardListView.adapter = gboardRVAdapter
        //클릭시 게시판 이동//
        binding.gotoBoardListView.setOnItemClickListener { parent, view, position, id->
            val intent = Intent(context, GroupMainActivity::class.java)
            intent.putExtra("key", boardKeyList[position])
            startActivity(intent)
        }

        /////////////////////////////////////////////
        binding.matchingBtn.setOnClickListener{
            val intent = Intent(context, Matching::class.java)
            startActivity(intent)
        }

        getFBBoardData()

        return binding.root
    }

    private fun getFBBoardData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    Log.d("homefragment", dataModel.toString())
//                    dataModel.key

                    val item = dataModel.getValue(GroupDataModel::class.java)

                    if (item != null) {
                        if(item.member?.contains(FBAuth.getUid()) == true){
                            boardDataList.add(item!!)
                            boardKeyList.add(dataModel.key.toString())
                        }
                    }

                    //boardDataList.add(item!!)
                    //boardKeyList.add(dataModel.key.toString())

                }

                boardKeyList.reverse()
                boardDataList.reverse()
                gboardRVAdapter.notifyDataSetChanged()

                Log.d("homefragment", boardDataList.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("homefragment", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.addValueEventListener(postListener)

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