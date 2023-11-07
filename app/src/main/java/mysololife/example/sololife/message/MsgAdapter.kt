package mysololife.example.sololife.message

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mysololife.R
import mysololife.example.sololife.utils.FBAuth

class MsgAdapter(val context : Context, val items : MutableList<MsgModel>) : BaseAdapter() {
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

        var convertView = convertView
        if (convertView == null) {

            convertView = LayoutInflater.from(parent?.context).inflate(R.layout.list_view_item2, parent, false)

        }

        val nicknameArea2 = convertView!!.findViewById<TextView>(R.id.listViewItemNicknameArea2)
        val textArea2 = convertView!!.findViewById<TextView>(R.id.listViewItemNickname2)

        nicknameArea2.text = items[position].senderInfo
        textArea2.text = items[position].sendTxt

        return convertView!!

    }
}