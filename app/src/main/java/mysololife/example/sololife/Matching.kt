package mysololife.example.sololife

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.launch
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.slider.CardStackAdapter
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import mysololife.example.sololife.utils.MyInfo


class Matching : AppCompatActivity() {

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var manager: CardStackLayoutManager

    private val usersDataList = mutableListOf<UserDataModel>()

    private val delList = mutableListOf<String>()

    private val TAG = "Matching"

    private var userCount =0

    private lateinit var currentUserGender : String
    private lateinit var currentUserUid : String

    private val uid = FirebaseAuthUtils.getUid()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)

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
                    getUserDataList(currentUserUid)
                    Toast.makeText(this@Matching,"모든 사용자가 지나갔습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCardRewound() {
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

        getOtherUserLikeList(otherUid)
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
}