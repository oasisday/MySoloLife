package mysololife.example.sololife.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibrationEffect.createWaveform
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentStudyteamBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mysololife.example.sololife.MainActivity
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.chatlist.ChatActivity
import mysololife.example.sololife.chatlist.ChatActivity2
import mysololife.example.sololife.chatlist.ChatRoomItem
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.group.GroupQnAActivity
import mysololife.example.sololife.map.Person
import mysololife.example.sololife.ui.StudyTeamAdapter
import mysololife.example.sololife.ui.TeamFaceAdapter
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard
import mysololife.example.sololife.utils.FirebaseRef
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

class GroupMainFragment : Fragment(),TeamFaceAdapter.OnItemClickListener {

    private val TAG = GroupMainFragment::class.java.simpleName
    private lateinit var binding: FragmentStudyteamBinding
    private lateinit var teamfaceAdapter: TeamFaceAdapter
    private lateinit var key: String
    private lateinit var gname: String
    private lateinit var vibrator: Vibrator
    val personList = mutableListOf<Person>()
    var teamleader: String = ""
    val myUid = FBAuth.getUid()
    val myUID = Firebase.auth.currentUser?.uid.toString()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStudyteamBinding.inflate(inflater, container, false)
        key = arguments?.getString("amount").toString()
        getBoardData(key)
        getLeaderData(key)
        binding.groupboardBtn.setOnClickListener {
            val intent = Intent(requireContext(), GroupQnAActivity::class.java)
            intent.putExtra("gname", gname)
            intent.putExtra("key", key)
            startActivity(intent)
        }
        binding.chatBtn.setOnClickListener {
            val intent = Intent(context, ChatActivity2::class.java)
            intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID,key)
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID,gname)
            startActivity(intent)
        }
        binding.groupOutBtn.setOnClickListener {
            showDialog(myUid)
        }
        getTeamFaceData(key)
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        return binding.root
    }

    private fun getLeaderData(key: String) {
        val reference = FBboard.boardInfoRef.child(key)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    teamleader =
                        dataSnapshot.child("leader").getValue(String::class.java).toString()
                } else {
                    println("Data does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 읽기 실패 시
                println("Error getting data: $databaseError")
            }
        })
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
                                    teamfaceAdapter = TeamFaceAdapter(
                                        requireContext(),
                                        personList,
                                        teamleader,
                                        this@GroupMainFragment
                                    )
                                    binding.faceRecyclerview.apply {
                                        adapter = teamfaceAdapter
                                        layoutManager = LinearLayoutManager(
                                            context,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
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
            val dataSnapshot = Firebase.database.getReference("Person").child(uid).get()
                .await() // userInfoRef는 Person 데이터가 있는 위치로 변경해야 합니다.
            val person = dataSnapshot.getValue(Person::class.java)
            person
        } catch (e: Exception) {
            null
        }
    }

    override fun onItemClick(position: Int) {
        Log.d("lastpang", personList[position].name.toString())
        profileDialog(position)
    }

    private fun profileDialog(p0: Int) {
        val mDialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.teamprofile_dialog, null)
        val mBuilder = AlertDialog.Builder(requireContext())
            .setView(mDialogView)
            .setTitle("사용자 정보")
            .setNegativeButton("종료") { dialog, _ ->
            dialog.dismiss() // 다이얼로그 닫기
        }

        val alertDialog = mBuilder.show()
        //지금 유저
        val currentUser = personList[p0]
        alertDialog.findViewById<TextView>(R.id.nameArea).text = currentUser.name
        if (!currentUser.profilePhoto.isNullOrEmpty()) {
            val myImage = alertDialog.findViewById<ImageView>(R.id.imageArea)
            // Glide를 사용하여 이미지 로드 및 설정
            Glide.with(requireContext())
                .load(currentUser.profilePhoto)
                .into(myImage)
        }


        //시비 걸기
        alertDialog.findViewById<ImageView>(R.id.sibiBtn).setOnClickListener {
            makeVibrate()
            getUserTokenByUID(currentUser.uid!!) { token ->
                if (token != null) {
                    Toast.makeText(requireContext(),"\""+currentUser.name+"\"님을 찔렀습니다!",Toast.LENGTH_SHORT).show()
                    // 여기에서 원하는 동작 수행
                    val client = OkHttpClient()
                    val root = JSONObject()
                    val data = JSONObject()
                    val notification = JSONObject()
                    val message =
                        " 스터디 그룹에서 누군가 당신을 찔렀습니다!!!"
                    notification.put("title", "스터디원 찌르기")
                    notification.put("body", "\"$gname\"" + message)
                    data.put("vibrate",true)
                    root.put("to", token)
                    root.put("priority", "high")
                    root.put("notification", notification)
                    root.put("data",data)
                    Log.d("testApi", root.toString())
                    val requestBody =
                        root.toString()
                            .toRequestBody("application/json; charset=utf-8".toMediaType())
                    val request =
                        Request.Builder().post(requestBody)
                            .url("https://fcm.googleapis.com/fcm/send")
                            .header(
                                "Authorization",
                                "key=${getString(R.string.fcm_server_key)}"
                            )
                            .build()
                    Log.d("testApi", request.toString())
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.stackTraceToString()
                        }

                        override fun onResponse(call: Call, response: Response) {
                        }
                    })
                }
            }
        }
        //메시지 창으로 넘어가기
        alertDialog.findViewById<Button>(R.id.msgBtn).setOnClickListener {
            val username = currentUser.name?: ""
            val otheruid = currentUser.uid?: ""
            if(otheruid == myUID){
                Toast.makeText(requireContext(),"본인이 아닌 다른 사람에게 메시지를 보내세요",Toast.LENGTH_SHORT).show()
            }
            else {
                if (!username.isNullOrEmpty() && !otheruid.isNullOrEmpty()) {
                    val chatRoomDB =
                        Firebase.database.reference.child(Key.DB_CHAT_ROOMS).child(myUID)
                            .child(otheruid)

                    chatRoomDB.get().addOnSuccessListener {
                        var chatRoomId = ""
                        if (it.value != null) {
                            //데이터가 존재
                            val chatRoom = it.getValue(ChatRoomItem::class.java)
                            chatRoomId = chatRoom?.chatRoomId ?: ""
                        } else {
                            chatRoomId = UUID.randomUUID().toString()
                            val newChatRoom = ChatRoomItem(
                                chatRoomId = chatRoomId,
                                otherUserName = username,
                                otherUserId = otheruid,
                            )
                            chatRoomDB.setValue(newChatRoom)
                        }

                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId)
                        intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otheruid)
                        startActivity(intent)
                        alertDialog.dismiss()
                    }
                }
            }
        }
        alertDialog.findViewById<Button>(R.id.addBtn).setOnClickListener {
            val guid = currentUser.uid!!
            checkUid(myUID, guid) { result ->
                if (result) {
                    Toast.makeText(
                        requireContext(),
                        "이미 추가된 친구입니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if(myUID == guid) Toast.makeText(requireContext(),"본인입니다.",Toast.LENGTH_SHORT).show()
                    else{
                        FirebaseRef.userBothRef.child(myUID).child(guid).setValue("true")
                        userLikeOtherUser(myUID, guid)
                        Toast.makeText(
                            requireContext(),
                            currentUser.name+ "님에게 친구 요청을 보냈습니다. 상대방이 요청을 받으면 친구 추가가 완료됩니다:)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            alertDialog.dismiss()
        }
    }

    private fun makeVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0 (Oreo) 이상에서는 VibrationEffect를 사용
            val vibrationEffect = VibrationEffect.createOneShot(300, 255)
            for (i in 0 until 2) {
                vibrator.vibrate(vibrationEffect)
                Thread.sleep(100)
            }
            vibrator.cancel()

        } else {
            // Android 8.0 미만에서는 deprecated된 vibrate 메서드 사용
            vibrator.vibrate(1000)
        }
    }

    private fun userLikeOtherUser(myUid: String, otherUid: String) {
        FirebaseRef.userLikeRef.child(myUid).child(otherUid).setValue("true")
        //todo 여기 firebase 함수 getOtherUserLikeList(otherUid) 추후에 고려하기
    }

    private fun checkUid(uid1: String, uid2: String, callback: (Boolean) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var check = false

                for (dataModel in dataSnapshot.children) {
                    val data = dataModel.key.toString()
                    if (data == uid2) {
                        check = true
                        break
                    }
                }
                // 데이터를 확인한 후에 콜백 호출
                callback(check)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userLikeRef.child(uid1).addListenerForSingleValueEvent(postListener)
    }
    private fun getUserTokenByUID(uid: String, callback: (String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        usersRef.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val token = snapshot.child("fcmToken").getValue(String::class.java)
                callback(token)
            } else {
                callback(null)
            }
        }.addOnFailureListener { exception ->
            Log.e("maptest", "Failed to read data from database: $exception")
            callback(null)
        }
    }
}
