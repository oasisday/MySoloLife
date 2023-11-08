package mysololife.example.sololife.group

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mysololife.R
import mysololife.example.sololife.utils.FBAuth

class GroupboardLVAdapter(private val boardList: List<GroupDataModel>) : BaseAdapter() {

    override fun getCount(): Int {
        return boardList.size
    }

    override fun getItem(position: Int): Any {
        return boardList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 특정 조건에 따라 항목을 비활성화할 함수
    private fun shouldDisableItem(position: Int): Boolean {
        return boardList[position].member?.contains(FBAuth.getUid()) == true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view = convertView
        view = LayoutInflater.from(parent?.context).inflate(R.layout.gboard_list_item,parent,false)
        val itemLinearLayoutView = view?.findViewById<LinearLayout>(R.id.gitemView)


        view = LayoutInflater.from(parent?.context).inflate(R.layout.gboard_list_item, parent, false)
        val title = view?.findViewById<TextView>(R.id.gtitleArea)
        val content = view?.findViewById<TextView>(R.id.gcontentArea)

        //내가 리더인 게시판이면
        if(boardList[position].leader == FBAuth.getUid()){
            itemLinearLayoutView?.setBackgroundColor(Color.parseColor("#BCC6CC"))
        }

        // Set the title and content
        title!!.text = boardList[position].classname
        content!!.text = boardList[position].classinfo


        return view!!
    }
}