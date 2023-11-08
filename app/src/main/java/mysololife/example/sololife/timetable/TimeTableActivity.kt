package mysololife.example.sololife.timetable


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mysololife.databinding.FragmentTimetableBinding
import com.islandparadise14.mintable.model.ScheduleEntity
import com.islandparadise14.mintable.tableinterface.OnScheduleClickListener
import com.islandparadise14.mintable.tableinterface.OnScheduleLongClickListener
import mysololife.example.sololife.dashboard.LectureMainActivity
import mysololife.example.sololife.dashboard.MainDashboardActivity


class TimeTableActivity : AppCompatActivity() {
    lateinit var binding: FragmentTimetableBinding
    private val day = arrayOf("월", "화", "수", "목", "금", "토")
    val scheduleList: ArrayList<ScheduleEntity> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.table.setOnScheduleClickListener(object : OnScheduleClickListener {
            override fun scheduleClicked(entity: ScheduleEntity) {
                Intent(this@TimeTableActivity, LectureMainActivity::class.java).apply {
                    putExtra("lecturename",entity.scheduleName)
                    startActivity(this)
                }
            }
        })

        binding.tableList.setOnClickListener {
            Intent(this, MainDashboardActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.table.setOnScheduleLongClickListener(object : OnScheduleLongClickListener {
            override fun scheduleLongClicked(entity: ScheduleEntity) {
                delete(entity)
                scheduleList.removeIf { it.scheduleName == entity.scheduleName }
                runOnUiThread {
                    binding.table.initTable(day)
                    binding.table.updateSchedules(scheduleList)
                }
            }
        })
        binding.lectureAdd.setOnClickListener {
            Intent(this, LectureInitActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        Thread {
            val infoEntities = AppDatabase.getInstance(this)?.infoDao()?.getAll() ?: emptyList()
            val scheduleEntities = infoEntities.map { it.toScheduleEntity() }
            scheduleList.addAll(scheduleEntities)
        }.start()
        binding.table.updateSchedules(scheduleList)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        binding.table.initTable(day)
        binding.table.updateSchedules(scheduleList)
    }

    private fun delete(entity: ScheduleEntity) {
        Thread {
            entity?.let { word ->
                AppDatabase.getInstance(this)?.infoDao()?.deleteByScheduleName(word.scheduleName)
                runOnUiThread {
                    Toast.makeText(this, "과목 삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}