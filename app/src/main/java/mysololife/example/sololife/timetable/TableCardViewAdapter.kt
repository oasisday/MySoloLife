package mysololife.example.sololife.timetable

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import mysololife.example.sololife.dashboard.DashboardAdapter

class TableCardViewAdapter(val infos: MutableList<InfoEntity>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<TableCardViewAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val lectureTitle: TextView = itemView.findViewById(R.id.lectureNametwo)
        val lectureProfessor: TextView = itemView.findViewById(R.id.lectureName2two)
        val lectureColor: View = itemView.findViewById(R.id.lectureColortwo)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // 다른 뷰(아이템)을 클릭했을 때의 동작
                listener.onItemClickListener(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_lecture_list2, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return infos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.lectureTitle.text = infos[position].scheduleName
        holder.lectureProfessor.text = infos[position].roomInfo + "강의실"
        val colorCode = infos[position].backgroundColor
        val colorInt = Color.parseColor(colorCode)
        holder.lectureColor.setBackgroundColor(colorInt)
        holder.lectureProfessor.setTextColor(colorInt)
    }
}

interface OnItemClickListener {
    fun onItemClickListener(position: Int)
}