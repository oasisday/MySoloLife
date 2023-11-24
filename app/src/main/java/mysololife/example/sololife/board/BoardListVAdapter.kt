package mysololife.example.sololife.board

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mysololife.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.group.board.GBoardInsideActivity
import mysololife.example.sololife.utils.FirebaseRef

class BoardListLVAdapter(private val boardList : MutableList<BoardModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return boardList.size
    }

    override fun getItem(position: Int): Any {
        return boardList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view = convertView

        view = LayoutInflater.from(parent?.context).inflate(R.layout.gboard_list_item,parent,false)


        val itemLinearLayoutView = view?.findViewById<LinearLayout>(R.id.itemView)
        val title = view?.findViewById<TextView>(R.id.gtitleArea)
        val content = view?.findViewById<TextView>(R.id.gcontentArea)
        val time = view?.findViewById<TextView>(R.id.gtimeArea)

        if(boardList[position].uid.equals(FBAuth.getUid())){
            itemLinearLayoutView?.setBackgroundColor(Color.parseColor("#BCC6CC"))
        }

        title!!.text = boardList[position].title
        time!!.text = boardList[position].time


        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                content!!.text = data!!.nickname.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }

        FirebaseRef.userInfoRef.child(boardList[position].uid).addValueEventListener(postListener)

        return view!!
    }



}