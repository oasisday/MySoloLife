package mysololife.example.sololife.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import mysololife.example.sololife.Constants.Companion.LOGCHECK
import mysololife.example.sololife.fragments.HomeFragment
import mysololife.example.sololife.group.GroupDataModel


class StudyTeamAdapter(val boardList: List<GroupDataModel>, var listener: HomeFragment) : RecyclerView.Adapter<StudyTeamAdapter.ViewHolder>() {

    // 뷰홀더 클래스 정의
   inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener{
        val title: TextView = itemView.findViewById(R.id.studyName)
        val content: TextView = itemView.findViewById(R.id.studyInfo)
        val imageView: ImageView = itemView.findViewById(R.id.studyroomBG)

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) listener.onItemClickListener(position)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.card_my_study, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return boardList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = boardList[position].classname
        holder.content.text = boardList[position].classinfo

        when (position % 6) {
            0 -> holder.imageView.setImageResource(R.drawable.cloud1)
            1 -> holder.imageView.setImageResource(R.drawable.studyroombg2)
            2 -> holder.imageView.setImageResource(R.drawable.studyroombg3)
            3 -> holder.imageView.setImageResource(R.drawable.studyroombg4)
            4 -> holder.imageView.setImageResource(R.drawable.studyroombg5)
            5 -> holder.imageView.setImageResource(R.drawable.studyroombg6)
            // Add more cases as needed
        }
    }
}

interface OnItemClickListener {
    fun onItemClickListener(position: Int)
}