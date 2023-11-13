package mysololife.example.sololife.group

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityGroupQnAactivityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.group.board.GBoardWriteActivity
import mysololife.example.sololife.group.board.InsideGBoardAdapter
import mysololife.example.sololife.utils.FBboard

class GroupQnAActivity : AppCompatActivity() {

    // data binding
    private lateinit var binding : ActivityGroupQnAactivityBinding

    // 받아온 key값
    private lateinit var key:String

    //TAG
    private var TAG = GroupQnAActivity::class.java.simpleName

    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()

    private lateinit var boardRVAdapter : InsideGBoardAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userListView = findViewById<ListView>(R.id.userListView)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_group_qn_aactivity)

        key = intent.getStringExtra("key").toString()

        binding.writeBtn.setOnClickListener{
            //startActivity(Intent(this, GBoardWriteActivity::class.java))

            val intent = Intent(this, GBoardWriteActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        boardRVAdapter = InsideGBoardAdapter(boardDataList)
        binding.gboardListview.adapter = boardRVAdapter

        getFBBoardData(key)

    }

    private fun getFBBoardData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    for (insidedataModel in dataModel.children) {
                        Log.d(TAG, insidedataModel.toString())
//                    dataModel.key

                        val item = insidedataModel.getValue(BoardModel::class.java)
                        boardDataList.add(item!!)
                        boardKeyList.add(dataModel.key.toString())
                    }
                }

//                boardKeyList.reverse()
//                boardDataList.reverse()
                boardRVAdapter.notifyDataSetChanged()

                Log.d(TAG, boardDataList.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.insideboardRef.child(key).addValueEventListener(postListener)
    }
}