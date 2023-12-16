package mysololife.example.sololife.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R

import mysololife.example.sololife.timetable.InfoEntity

class DashboardAdapter(val infos: MutableList<InfoEntity>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val lectureTitle: TextView = itemView.findViewById(R.id.lectureName)
        val lectureProfessor: TextView = itemView.findViewById(R.id.lectureName2)
        val lectureColor: View = itemView.findViewById(R.id.lectureColor)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val smallbtn: ImageView = itemView.findViewById(R.id.cardviewSetBtn)

        init {
            itemView.setOnClickListener(this)
            smallbtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val itemName = infos[position].scheduleName
                val itemview = itemView.findViewById<View>(R.id.cardviewSetBtn)
                when (v?.id) {
                    R.id.cardviewSetBtn -> {
                        // 버튼을 눌렀을 때의 동작
                        listener.onButtonClickListener(position,itemName,itemview)
                    }

                    else -> {
                        // 다른 뷰(아이템)을 클릭했을 때의 동작
                        listener.onItemClickListener(position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_lecture_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return infos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.lectureTitle.text = infos[position].scheduleName
        holder.lectureProfessor.text = "강의실 : "+infos[position].roomInfo
        val colorCode = infos[position].backgroundColor
        val colorInt = Color.parseColor(colorCode)
        holder.lectureColor.setBackgroundColor(colorInt)
        holder.lectureProfessor.setTextColor(colorInt)
    }
}

interface OnItemClickListener {
    fun onItemClickListener(position: Int)
    fun onButtonClickListener(position: Int, itemName: String,itemView: View)
}