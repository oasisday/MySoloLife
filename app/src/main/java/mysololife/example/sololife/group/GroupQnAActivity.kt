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
import mysololife.example.sololife.group.board.GBoardInsideActivity
import mysololife.example.sololife.group.board.GBoardWriteActivity
import mysololife.example.sololife.group.board.InsideGBoardAdapter
import mysololife.example.sololife.utils.FBboard

class GroupQnAActivity : AppCompatActivity() {

    // data binding
    private lateinit var binding : ActivityGroupQnAactivityBinding

    // 받아온 key값
    private lateinit var key:String

    // 받아온 그룹 이름 값
    private lateinit var gname:String

    //TAG
    private var TAG = GroupQnAActivity::class.java.simpleName

    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()

    private lateinit var boardRVAdapter : InsideGBoardAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_group_qn_aactivity)

        key = intent.getStringExtra("key").toString()

        binding.writeBtn.setOnClickListener{
            //startActivity(Intent(this, GBoardWriteActivity::class.java))

            val intent = Intent(this, GBoardWriteActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        gname = intent.getStringExtra("gname").toString()

        boardRVAdapter = InsideGBoardAdapter(boardDataList)
        binding.gboardListview.adapter = boardRVAdapter
        binding.qnainsideName.setText(" \""+gname+"\"  자유게시판")

        getFBBoardData(key)
        Log.d("abc",key)

        binding.gboardListview.setOnItemClickListener{parent, view, position, id ->
            val intent = Intent(this, GBoardInsideActivity::class.java)
            intent.putExtra("title", boardDataList[position].title)
            intent.putExtra("content", boardDataList[position].content)
            intent.putExtra("time", boardDataList[position].time)
            intent.putExtra("uid",boardDataList[position].uid)
            intent.putExtra("bkey", boardDataList[position].bkey)
            intent.putExtra("bname",gname)
            intent.putExtra("key",key)

            startActivity(intent)
        }

    }

    private fun getFBBoardData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    Log.d(TAG, dataModel.toString())
//                    dataModel.key

                    val item = dataModel.getValue(BoardModel::class.java)
                    boardDataList.add(item!!)
                    boardKeyList.add(dataModel.key.toString())
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