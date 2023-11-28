package mysololife.example.sololife.alarm

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import com.example.mysololife.databinding.ItemRecordalarmBinding
import de.coldtea.smplr.smplralarm.smplrAlarmCancel
import mysololife.example.sololife.fragments.HomeFragment
import mysololife.example.sololife.group.GroupDataModel

class AlarmListAdapter(val context: Context, val alarmList: MutableList<AlarmItem>) : BaseAdapter() {
    private var mBinding: ItemRecordalarmBinding? = null
    private val binding get() = mBinding!!
    val sharedPrefs = context.getSharedPreferences(RECORD_NAME, Context.MODE_PRIVATE)
    override fun getView(position: Int, covertView: View?, parent: ViewGroup?): View {

        mBinding = ItemRecordalarmBinding.inflate(LayoutInflater.from(context))

        val lectureNameTextView: TextView = binding.lectureNameTextView
        val dayOfWeekTextView: TextView = binding.dayOfWeekTextView
        val startTimeTextView: TextView = binding.startTimeTextView
        val cancelImageView: ImageView = binding.cancelImageView
        val alarmItem = alarmList[position]

        cancelImageView.setOnClickListener {
            onCancelClick(alarmItem)
        }

        val requestId = alarmItem.requestId.toString()

        // Check if the RequestId exists in SharedPreferences
        if (sharedPrefs.contains(requestId)) {
            // If it exists, retrieve data using the RequestId as the key
            val lecture = sharedPrefs.getString(requestId, null)
            lectureNameTextView.text = lecture
        }
        else lectureNameTextView.text = alarmItem.requestId.toString()

        dayOfWeekTextView.text = getFormattedWeekdays(alarmItem.weekDays)
        startTimeTextView.text = getFormattedTime(alarmItem.hour, alarmItem.minute)
        return mBinding!!.root
    }

    override fun getItem(position: Int): Any {
        return alarmList[position]
    }

    override fun getCount(): Int {
        return alarmList.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private fun getFormattedWeekdays(weekdays: List<String>): String {
        // 주중 문자열 생성 (예: 월, 화, 수)
        return weekdays.joinToString(", ") { getDayInitial(it) }
    }

    private fun getDayInitial(day: String): String {
        // 주중 문자열을 간단하게 표시 (예: 월요일 -> 월)
        return when (day) {
            "SUNDAY" -> "일요일"
            "MONDAY" -> "월요일"
            "TUESDAY" -> "화요일"
            "WEDNESDAY" -> "수요일"
            "THURSDAY" -> "목요일"
            "FRIDAY" -> "금요일"
            "SATURDAY" -> "토요일"
            else -> ""
        }
    }

    private fun getFormattedTime(hour: Int, minute: Int): String {
        // 시간 및 분을 HH:mm 형식으로 반환
        return String.format("%02d:%02d", hour, minute)
    }


    private fun onCancelClick(alarmItem: AlarmItem) {
        Log.d("alarmtest",alarmItem.requestId.toString()+"클릭 성공")
        alarmList.remove(alarmItem)
        smplrAlarmCancel(context) {
            requestCode { alarmItem.requestId.toInt() }
        }
        notifyDataSetChanged()
    }
}