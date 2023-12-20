package mysololife.example.sololife.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentTalkBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.board.BoardInsideActivity
import mysololife.example.sololife.board.BoardListLVAdapter
import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.board.BoardWriteActivity
import mysololife.example.sololife.utils.FBRef

class GroupMakeFragment : Fragment() {

    private lateinit var binding : FragmentTalkBinding

    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()

    private val TAG = GroupMakeFragment::class.java.simpleName

    private lateinit var boardRVAdapter : BoardListLVAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_talk, container, false)

        boardRVAdapter = BoardListLVAdapter(boardDataList)
        binding.boardListView.adapter = boardRVAdapter

        binding.boardListView.setOnItemClickListener { parent, view, position, id ->

            val intent = Intent(context, BoardInsideActivity::class.java)
            intent.putExtra("key", boardKeyList[position])
            startActivity(intent)

        }

        binding.writeBtn.setOnClickListener{
            val intent = Intent(context, BoardWriteActivity::class.java)
            startActivity(intent)
        }

        getFBBoardData()

        return binding.root
    }


    private fun getFBBoardData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardDataList.clear()
                boardKeyList.clear()
                val normalPostList = mutableListOf<BoardModel>()
                val normalPostKeyList = mutableListOf<String>()

                var count = 0  // 공지사항 카운터
                for (dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(BoardModel::class.java)
                    if (count < 5) {
                        boardDataList.add(0, item!!)
                        boardKeyList.add(0, dataModel.key.toString())
                    } else {
                        normalPostList.add(item!!)  // 일반 게시물은 별도의 리스트에 추가
                        normalPostKeyList.add(dataModel.key.toString())  // key도 추가
                    }
                    count++
                }

                normalPostList.reverse()
                normalPostKeyList.reverse()

                boardDataList.addAll(normalPostList)
                boardKeyList.addAll(normalPostKeyList)

                boardRVAdapter.notifyDataSetChanged()

                Log.d(TAG, boardDataList.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.addValueEventListener(postListener)

    }



}