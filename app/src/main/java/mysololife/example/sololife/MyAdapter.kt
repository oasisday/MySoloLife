package mysololife.example.sololife

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.mysololife.R

class MyAdapter(private val context : Activity, private val arrayList : ArrayList<User>) : ArrayAdapter<User>(context,
    R.layout.item_chatroom, arrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = inflater.inflate(R.layout.item_chatroom, null)

        val imageView : ImageView = view.findViewById(R.id.profile_img)
        val username : TextView = view.findViewById(R.id.nicknameTextView)
        val lastMsg : TextView = view.findViewById(R.id.lastMessageTextView)
        val lastMsgtime : TextView = view.findViewById(R.id.chatTime)

        imageView.setImageResource(arrayList[position].imageId)
        username.text = arrayList[position].name
        lastMsg.text = arrayList[position].lastMessage
        lastMsgtime.text = arrayList[position].lastMsgTime


        return view
    }
}
