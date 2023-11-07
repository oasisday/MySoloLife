package mysololife.example.sololife.message

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.example.mysololife.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef

class MyMsgActivity : AppCompatActivity() {

    val TAG = "MyMsgActivity"

    lateinit var listViewAdapter: MsgAdapter
    val msgList = mutableListOf<MsgModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_msg)

        val listview = findViewById<ListView>(R.id.msgListView)

        listViewAdapter = MsgAdapter(this, msgList)
        listview.adapter = listViewAdapter

        getMyMsg()
    }

    private fun getMyMsg(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                msgList.clear()

                for (dataModel in dataSnapshot.children){
                    val msg = dataModel.getValue(MsgModel::class.java)
                    msgList.add(msg!!)
                }

                //최신순으로 정렬
                msgList.reverse()

                listViewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userMsgRef.child(FirebaseAuthUtils.getUid()).addValueEventListener(postListener)
    }

}