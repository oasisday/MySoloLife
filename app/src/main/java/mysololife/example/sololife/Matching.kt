package mysololife.example.sololife

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.launch
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.chatlist.ChatActivity
import mysololife.example.sololife.chatlist.ChatRoomItem
import mysololife.example.sololife.map.Person
import mysololife.example.sololife.slider.CardStackAdapter
import mysololife.example.sololife.ui.TeamFaceAdapter
import mysololife.example.sololife.utils.FBboard.Companion.database
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import mysololife.example.sololife.utils.MyInfo
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


class Matching : AppCompatActivity(),TeamFaceAdapter.OnItemClickListener  {

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var manager: CardStackLayoutManager
    private val usersDataList = mutableListOf<UserDataModel>()
    private val delList = mutableListOf<String>()
    private val TAG = "Matching"
    private var userCount =0
    private lateinit var vibrator: Vibrator
    val personList = mutableListOf<Person>()
    private lateinit var teamfaceAdapter: TeamFaceAdapter
    private lateinit var currentUserGender : String
    private lateinit var currentUserUid : String
    val myUID = Firebase.auth.currentUser?.uid.toString()

    private val uid = FirebaseAuthUtils.getUid()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)
        val faceRecyclerView = findViewById<RecyclerView>(R.id.faceRecyclerview)
        //사용자 표시

        val personRef = database.getReference("Person")
        val outbtn = findViewById<ImageView>(R.id.outbtn)
        outbtn.setOnClickListener { finish() }
        // 데이터 읽기 예제
        personRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (personSnapshot in dataSnapshot.children) {
                    val person = personSnapshot.getValue(Person::class.java)
                    if (person != null) {
                        personList.add(person)
                    }
                }
                // personList에 데이터가 추가된 후의 처리
                teamfaceAdapter = TeamFaceAdapter(
                    this@Matching,
                    personList,
                    "TiZWoCiBukMhRfwd7jBRpU7GZoE3",
                    this@Matching
                )

                faceRecyclerView.apply {
                    adapter = teamfaceAdapter
                    layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 읽기 중 오류 발생 시 처리
                // 예: 오류 로그 출력
                Log.e("FirebaseData", "데이터 읽기 중 오류 발생", databaseError.toException())
            }
        })
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        manager = CardStackLayoutManager(baseContext, object : CardStackListener{
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {

                if(direction == Direction.Right){
                    //userLikeOtherUser(uid,usersDataList[userCount].uid.toString())
                    //lightOverlay.bringToFront()

                    //checkUid(uid, usersDataList[userCount].uid.toString()) { result ->
                        Log.d("uidtest", "here")
                        userLikeOtherUser(uid, usersDataList[userCount].uid.toString())
                    //}

                }
                if(direction == Direction.Left){
                    //rightOverlay.bringToFront()
                }

                userCount = userCount + 1

                //다 넘겼으면//
                if(userCount == usersDataList.count()){
                    cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
                    cardStackView.layoutManager = manager
                    cardStackView.adapter = cardStackAdapter
                    cardStackView.adapter?.notifyDataSetChanged()
                    getUserDataList(currentUserUid)
                    Toast.makeText(this@Matching,"모든 사용자가 지나갔습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCardRewound() {
                cardStackView.adapter?.notifyDataSetChanged()
                Log.d("testing","rewound")
            }

            override fun onCardCanceled() {
            }

            override fun onCardAppeared(view: View?, position: Int) {
            }

            override fun onCardDisappeared(view: View?, position: Int) {
            }

        })

        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = manager
        cardStackView.adapter = cardStackAdapter

        lifecycleScope.launch {
            getMylikeList()
            getMyUserData()
            delList()
        }
    }


    private fun delList(){
        lifecycleScope.launch {

            val del = mutableListOf<UserDataModel>()

            for(tmp in usersDataList){
                if(delList.contains(tmp.uid)){
                    del.add(tmp)
                }
            }

            Log.d("aaa",del.toString())

            for(tmp in del){
                usersDataList.remove(tmp)
            }
        }
    }

    private fun getUserDataList(currentUserUid : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("matchingTest","getUserDataList 좋아요 누름 "+ dataSnapshot.toString())
                for (dataModel in dataSnapshot.children){

                    val user = dataModel.getValue(UserDataModel::class.java)

                    //본인 빼고
                    if(user!!.uid.toString().equals(currentUserUid)){

                    }
                    else{
                        usersDataList.add(user!!)
                        //del(user.uid.toString(),user)
                    }
                }

                //del()
                if(usersDataList.isNotEmpty())
                    cardStackAdapter.notifyDataSetChanged()
                else
                    Toast.makeText(this@Matching, "모든 사용자가 지나갔습니다.",Toast.LENGTH_SHORT).show()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)
    }


    private fun del(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){
                    val likeUserKey = dataModel.key.toString()

                    //usersDataList에서 찾아서 삭제
                    for(tmp in usersDataList){
                        if(tmp.uid == likeUserKey){
                            usersDataList.remove(tmp)
                            break
                        }
                    }

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(currentUserUid).addValueEventListener(postListener)

    }

    private fun is_like(uid : String) {

        var ans = true

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val data = dataSnapshot.value.toString()

                Log.d("abc",data)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)

    }

    private fun getMylikeList(){

        delList.clear()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val data = dataSnapshot.value.toString()
                delList.add(data)
                Log.d("matchingTest","getMylikeList 좋아요 누름 "+ data)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)

    }

    private fun getMyUserData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("matchingTest","getMyUserData좋아요 누름 "+ dataSnapshot.toString())
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                Log.d(TAG, data?.gender.toString())

                currentUserGender = data?.gender.toString()
                currentUserUid = data?.uid.toString()

                MyInfo.myNickname = data?.nickname.toString()

                getUserDataList(currentUserUid)
                //del()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }
    //나의 Uid//상대의 Uid
    private fun userLikeOtherUser(myUid : String, otherUid : String){
        FirebaseRef.userLikeRef.child(myUid).child(otherUid).setValue("true")

        //getOtherUserLikeList(otherUid)
    }
    private fun getOtherUserLikeList(otherUid: String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){
                    val likeUserKey = dataModel.key.toString()
                    if(likeUserKey.equals(uid)){
                        Toast.makeText(this@Matching,"matching success!!", Toast.LENGTH_SHORT).show()

                        FirebaseRef.userBothRef.child(uid).child(otherUid).setValue("true")
                        FirebaseRef.userBothRef.child(otherUid).child(uid).setValue("true")
                        FirebaseRef.userLikeRef.child(uid).child(otherUid).setValue("false")
                        FirebaseRef.userLikeRef.child(otherUid).child(uid).setValue("false")
                        //createNotificationChannel()
                        //sendNotification()
                    }

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)
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

    override fun onItemClick(position: Int) {
        Log.d("lastpang", personList[position].name.toString())
        profileDialog(position)
    }
    private fun profileDialog(p0: Int) {
        val mDialogView =
            LayoutInflater.from(this).inflate(R.layout.teamprofile_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
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
            Glide.with(this)
                .load(currentUser.profilePhoto)
                .into(myImage)
        }


        //시비 걸기
        alertDialog.findViewById<ImageView>(R.id.sibiBtn).setOnClickListener {
            makeVibrate()
            Toast.makeText(this,"찌르기 기능은 스터디 팀 내에서만 사용하실 수 있습니다 :)",Toast.LENGTH_SHORT).show()
        }
        //메시지 창으로 넘어가기
        alertDialog.findViewById<Button>(R.id.msgBtn).setOnClickListener {
            val username = currentUser.name?: ""
            val otheruid = currentUser.uid?: ""
            if(otheruid == myUID){
                Toast.makeText(this,"본인이 아닌 다른 사람에게 메시지를 보내세요",Toast.LENGTH_SHORT).show()
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

                        val intent = Intent(this, ChatActivity::class.java)
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
                        this,
                        "이미 추가된 친구입니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if(myUID == guid) Toast.makeText(this,"본인입니다.",Toast.LENGTH_SHORT).show()
                    else{
                        FirebaseRef.userBothRef.child(myUID).child(guid).setValue("true")
                        userLikeOtherUser(myUID, guid)
                        Toast.makeText(
                            this,
                            currentUser.name+ "님에게 친구 요청을 보냈습니다. 상대방이 요청을 받으면 친구 추가가 완료됩니다:)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            alertDialog.dismiss()
        }
    }
    override fun onPause() {
        super.onPause()
        Glide.with(this).pauseRequests()
    }

    override fun onResume() {
        super.onResume()
        Glide.with(this).resumeRequests()
    }
}