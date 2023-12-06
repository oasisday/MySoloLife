package mysololife.example.sololife.group.board

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mysololife.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FirebaseRef

class InsideGBoardAdapter(val items: MutableList<BoardModel>): BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view = convertView

        view = LayoutInflater.from(parent?.context).inflate(R.layout.gboard_list_item,parent,false)


        val itemLinearLayoutView = view?.findViewById<LinearLayout>(R.id.itemView)
        val title = view?.findViewById<TextView>(R.id.gtitleArea)
        val content = view?.findViewById<TextView>(R.id.contentArea)
        val time = view?.findViewById<TextView>(R.id.gtimeArea)
        val name = view?.findViewById<TextView>(R.id.nameArea)


        if(items[position].uid.equals(FBAuth.getUid())){
            itemLinearLayoutView?.setBackgroundColor(Color.parseColor("#BCC6CC"))
        }

        title!!.text = items[position].title
        time!!.text = items[position].time
        content!!.text = items[position].content

        if(items[position].content.length>10){
            content!!.text = items[position].content.take(60)+"..."
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                name!!.text = data!!.nickname.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }

        FirebaseRef.userInfoRef.child(items[position].uid).addValueEventListener(postListener)


        return view!!
    }

}