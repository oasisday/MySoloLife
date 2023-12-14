package mysololife.example.sololife.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.map.Person
import mysololife.example.sololife.utils.FirebaseRef


class TeamFaceAdapter(private val context : Context, val items: List<Person>,val teamleaderUid:String,private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<TeamFaceAdapter.ViewHolder>() {
    // 뷰홀더 클래스 정의
    interface  OnItemClickListener{
        fun onItemClick(position: Int)
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.myName)
        val imageView: ImageView = itemView.findViewById(R.id.myImage)
        val cloud: ImageView = itemView.findViewById(R.id.imageCloud)
        val crown: ImageView = itemView.findViewById(R.id.leadercrown)
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            itemClickListener.onItemClick(adapterPosition)
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
        if (item.uid == teamleaderUid) {
            // item.uid가 teamleaderUid와 같다면
            holder.crown.visibility = View.VISIBLE
        } else {
            // item.uid가 teamleaderUid와 다르다면
            holder.crown.visibility = View.GONE
        }
        holder.name.text = item.name
        val colorResId = when (position % 6) {
            0 -> R.color.cloudcolor_zero
            1 -> R.color.cloudcolor_one
            3 -> R.color.cloudcolor_two
            4 -> R.color.cloudcolor_three
            5 -> R.color.cloudcolor_four
            2 -> R.color.cloudcolor_five
            else -> R.color.maincolor_one
        }

        // 배경색 변경
        val backgroundColor = ContextCompat.getColor(context, colorResId)
        holder.cloud.backgroundTintList = ColorStateList.valueOf(backgroundColor)

        holder.cloud.setOnClickListener {
            // 클릭 이벤트를 인터페이스를 통해 외부로 전달
            itemClickListener.onItemClick(position)
        }
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

