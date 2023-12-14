package mysololife.example.sololife.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.map.Person
import mysololife.example.sololife.utils.FirebaseRef

class TeamFaceAdapter(private val context : Context, val items: List<Person>) : RecyclerView.Adapter<TeamFaceAdapter.ViewHolder>() {
    // 뷰홀더 클래스 정의
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.myName)
        val imageView: ImageView = itemView.findViewById(R.id.myImage)
        override fun onClick(p0: View?) {
            //TODO("Not yet implemented")
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_teamface, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name

        val uid = item.uid

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(Person::class.java)
                holder.name.text = data?.name
                // 이미지 URL이 Person 객체에 저장되어 있다고 가정
                val imageUrl = data?.profilePhoto

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(imageUrl)
                        .into(holder.imageView)
                } else {
                    // 이미지 URL이 없거나 비어있을 경우의 처리
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 로딩 실패 시 처리
            }
        }

        if (uid != null) {
            FirebaseRef.personRef.child(uid).addValueEventListener(postListener)
        }
    }
}

