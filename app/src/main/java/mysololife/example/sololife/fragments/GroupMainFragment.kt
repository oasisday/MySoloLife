package mysololife.example.sololife.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentStudyteamBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mysololife.example.sololife.MainActivity
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.group.GroupQnAActivity
import mysololife.example.sololife.map.Person
import mysololife.example.sololife.ui.StudyTeamAdapter
import mysololife.example.sololife.ui.TeamFaceAdapter
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard

class GroupMainFragment : Fragment() {

    private val TAG = GroupMainFragment::class.java.simpleName
    private lateinit var binding: FragmentStudyteamBinding
    private lateinit var teamfaceAdapter: TeamFaceAdapter
    private lateinit var key: String
    private lateinit var gname: String

    val myUid = FBAuth.getUid()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStudyteamBinding.inflate(inflater, container, false)

        key = arguments?.getString("amount").toString()
        getBoardData(key)

        binding.groupboardBtn.setOnClickListener {
            val intent = Intent(requireContext(), GroupQnAActivity::class.java)
            intent.putExtra("gname", gname)
            intent.putExtra("key", key)
            startActivity(intent)
        }
        binding.chatBtn.setOnClickListener {
            Toast.makeText(requireContext(), "chat버튼을 클릭하였습니다.", Toast.LENGTH_SHORT).show()
        }
        binding.groupOutBtn.setOnClickListener {
            showDialog(myUid)
        }
        getTeamFaceData(key)

        return binding.root
    }

    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)

                    binding.studyNameTextView.text = dataModel!!.classname
                    binding.studyInfoTextView.text = dataModel!!.classinfo

                    gname = dataModel!!.classname.toString()
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                // Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)
    }

    private fun logoutGroup(uid: String) {
        val memberRef = FBboard.boardInfoRef.child(key).child("member")

        var arr: MutableList<String>? = ArrayList()
        var checked = true

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)
                    arr = dataModel!!.member

                    if (dataModel!!.leader.equals(myUid)) checked = false
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                // Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)

        if (checked) {
            arr?.remove(uid)
            memberRef.setValue(arr)
        } else {
            FBboard.boardInfoRef.child(key).removeValue()
        }
    }

    private fun showDialog(uid: String) {
        val mDialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_del, null)
        val mBuilder = AlertDialog.Builder(requireContext())
            .setView(mDialogView)
            .setTitle("정말 \"$gname\" 그룹에서 탈퇴 하시겠습니까?")

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener {
            Toast.makeText(requireContext(), "\"$gname\" 그룹에서 탈퇴되었습니다.", Toast.LENGTH_LONG).show()
            logoutGroup(uid)
            alertDialog.dismiss()
            //requireActivity().finish()

            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun getTeamFaceData(key: String) {
        val memberRef = FBboard.boardInfoRef.child(key).child("member")
        val personList = mutableListOf<Person>()

        memberRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                lifecycleScope.launch {
                    // 각 userID에 대한 Person 데이터 가져오기
                    for (userID in dataSnapshot.children) {
                        Log.d("boardTest", userID.toString())
                        userID.getValue(String::class.java)?.let {
                            // getPersonInfo 함수 호출
                            val person = getPersonInfo(it)

                            // 가져온 Person을 리스트에 추가
                            person?.let {
                                personList.add(it)
                                Log.d("boardTest", it.name.toString() + it.profilePhoto.toString())
                                // 모든 데이터를 가져왔을 때 원하는 작업 수행
                                if (personList.size.toLong() == dataSnapshot.childrenCount) {
                                    // 모든 데이터를 가져왔을 때 수행할 작업
                                    // 예: RecyclerView에 데이터 설정
                                    teamfaceAdapter = TeamFaceAdapter(requireContext(),personList)
                                    binding.faceRecyclerview.apply {
                                        adapter = teamfaceAdapter
                                        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                    }
                                }
                            }
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    suspend fun getPersonInfo(uid: String): Person? {
        return try {
            val dataSnapshot =  Firebase.database.getReference("Person").child(uid).get().await() // userInfoRef는 Person 데이터가 있는 위치로 변경해야 합니다.
            val person = dataSnapshot.getValue(Person::class.java)
            person
        } catch (e: Exception) {
            null
        }
    }
}
