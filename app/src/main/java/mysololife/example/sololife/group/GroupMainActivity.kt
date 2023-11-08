package mysololife.example.sololife.group

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityGroupMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard

class GroupMainActivity : AppCompatActivity() {

    private val TAG = GroupMainActivity::class.java.simpleName
    private lateinit var binding : ActivityGroupMainBinding

    private lateinit var key:String

    val myUid = FBAuth.getUid()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_group_main)

        key = intent.getStringExtra("key").toString()
        getBoardData(key)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_main)


    }

    private fun getBoardData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)

                    binding.studyName.text = dataModel!!.classname
                    binding.studyInfo.text = dataModel!!.classinfo

                }
                catch (e:Exception){
                    Log.d(TAG, "삭제완료")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)
    }
}