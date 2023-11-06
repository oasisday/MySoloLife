package mysololife.example.sololife.message

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import com.example.mysololife.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.message.fcm.NotiModel
import mysololife.example.sololife.message.fcm.PushNotification
import mysololife.example.sololife.message.fcm.RetrofitInstance
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import okhttp3.Dispatcher

class MyLikeListActivity : AppCompatActivity() {

    private val TAG = "MyLikeListActivity"
    private val uid = FirebaseAuthUtils.getUid()

    private val likeUserList = mutableListOf<UserDataModel>()
    private val likeUserListUid = mutableListOf<String>()

    lateinit var listviewAdapter : ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like_list)

        val userListView = findViewById<ListView>(R.id.userListView)

        listviewAdapter = ListViewAdapter(this, likeUserList)
        userListView.adapter = listviewAdapter

        //내가 좋아요한 사람들
        getMyLikeList()

        userListView.setOnItemClickListener{ parent, view, position, id ->

            checkMatching(likeUserList[position].uid.toString())

            val notiModel = NotiModel("a", "b")

            val pushModel = PushNotification(notiModel,likeUserList[position].token.toString())
            testPush(pushModel)
        }

    }

    private fun checkMatching(otherUid : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.children.count() == 0){
                    Toast.makeText(this@MyLikeListActivity,"매칭 실패", Toast.LENGTH_SHORT).show()
                }else{
                    for (dataModel in dataSnapshot.children){
                        val likeUserKey = dataModel.key.toString()
                        //매칭이 되어있는지
                        if(likeUserKey.equals(uid)){
                            Toast.makeText(this@MyLikeListActivity, "매칭 성공", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@MyLikeListActivity,"매칭 실패", Toast.LENGTH_SHORT).show()
                        }

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

    private fun getMyLikeList(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){
                    likeUserListUid.add(dataModel.key.toString())
                }

                getUserDataList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)
    }

    private fun getUserDataList() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){

                    val user = dataModel.getValue(UserDataModel::class.java)

                    if(likeUserListUid.contains(user?.uid)){
                        //내가 좋아요한 사람들의 정보 뽑음
                        likeUserList.add(user!!)
                    }
                }
                listviewAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)
    }

    private  fun testPush(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        RetrofitInstance.api.postNotification(notification)
    }

}