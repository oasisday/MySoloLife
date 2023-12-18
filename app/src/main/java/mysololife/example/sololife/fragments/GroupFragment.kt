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
import com.example.mysololife.databinding.FragmentShowBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.group.GroupMainActivity
import mysololife.example.sololife.group.GroupboardLVAdapter
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard


class GroupFragment : Fragment() {
    private lateinit var binding : FragmentShowBinding

    private val boardDataList = mutableListOf<GroupDataModel>()
    private val boardKeyList = mutableListOf<String>()

    private val TAG = GroupFragment::class.java.simpleName

    private lateinit var gboardRVAdapter: GroupboardLVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_show, container, false)

        gboardRVAdapter = GroupboardLVAdapter(boardDataList)

        binding.gotoBoardListView.adapter = gboardRVAdapter

        //클릭시 게시판 이동//
        binding.gotoBoardListView.setOnItemClickListener { parent, view, position, id->
            val intent = Intent(context, GroupMainActivity::class.java)
            intent.putExtra("key", boardKeyList[position])
            intent.putExtra("g_name", boardKeyList[position])
            startActivity(intent)
        }

        /////////////////////////////////////////////
        binding.CardBtn.setOnClickListener{
//            val intent = Intent(activity, MyLikeListActivity::class.java)
//            startActivity(intent)
        }

        getFBBoardData()

        return binding.root
    }

    private fun getFBBoardData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    Log.d(TAG, dataModel.toString())
//                    dataModel.key

                    val item = dataModel.getValue(GroupDataModel::class.java)

                    if (item != null) {
                        if(item.member?.contains(FBAuth.getUid()) == true){
                            boardDataList.add(item!!)
                            boardKeyList.add(dataModel.key.toString())
                        }
                    }

                    //boardDataList.add(item!!)
                    //boardKeyList.add(dataModel.key.toString())

                }

                boardKeyList.reverse()
                boardDataList.reverse()
                gboardRVAdapter.notifyDataSetChanged()

                Log.d(TAG, boardDataList.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.addValueEventListener(postListener)

    }

}