package mysololife.example.sololife.timetable

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentTimetableBinding
import com.google.android.material.snackbar.Snackbar
import com.islandparadise14.mintable.model.ScheduleEntity
import com.islandparadise14.mintable.tableinterface.OnScheduleClickListener
import com.islandparadise14.mintable.tableinterface.OnScheduleLongClickListener
import mysololife.example.sololife.dashboard.LectureMainActivity


class TimeTableActivity : AppCompatActivity(), OnItemClickListener {
    lateinit var binding: FragmentTimetableBinding
    private val day = arrayOf("월", "화", "수", "목", "금", "토")
    val scheduleList: ArrayList<ScheduleEntity> = ArrayList()
    private var lectureList = mutableListOf<InfoEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.table.setOnScheduleClickListener(object : OnScheduleClickListener {
            override fun scheduleClicked(entity: ScheduleEntity) {
//                Intent(this@TimeTableActivity, LectureMainActivity::class.java).apply {
//                    putExtra("lecturename",entity.scheduleName)
//                    startActivity(this)
//                }
                Snackbar.make(binding.root, entity.scheduleName+"과목을 클릭하였습니다. UI 목적으로 제작한 것으로 대시보드를 통해 개인 강의실로 접근하세요!", Snackbar.LENGTH_SHORT).show()
            }
        })

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


        Thread {
            val infoEntities = AppDatabase.getInstance(this)?.infoDao()?.getAllLecture() ?: emptyList()

            runOnUiThread {
                lectureList.addAll(infoEntities)
                //recyclerview 등록
                binding.cardviewrecyclerView.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.cardviewrecyclerView.adapter = TableCardViewAdapter(lectureList, this)
                binding.cardviewrecyclerView.adapter?.notifyDataSetChanged()

                if (lectureList.isEmpty()) {
                    binding.notificationTextView.visibility = View.VISIBLE
                } else {
                    binding.notificationTextView.visibility = View.GONE
                }
            }
        }.start()
    }
    override fun onResume() {
        super.onResume()
        Thread {
            val infoEntities = AppDatabase.getInstance(this)?.infoDao()?.getAll() ?: emptyList()
            val scheduleEntities = infoEntities.map { it.toScheduleEntity() }
            scheduleList.addAll(scheduleEntities)
        }.start()
        binding.table.updateSchedules(scheduleList)
        binding.cardviewrecyclerView.adapter?.notifyDataSetChanged()
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
                    Toast.makeText(this, "비밀 기능을 발견하셨네요:)\n과목 삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

        Thread{
            runOnUiThread {
                lectureList.clear()
                binding.cardviewrecyclerView.adapter?.notifyDataSetChanged()
            }
        }.start()
        Thread.sleep(300)
        Thread {
            val infoEntities =
                AppDatabase.getInstance(this)?.infoDao()?.getAllLecture() ?: emptyList()
            lectureList.addAll(infoEntities)
            runOnUiThread {
                binding.cardviewrecyclerView.adapter?.notifyDataSetChanged()
            }
        }.start()
    }

    override fun onItemClickListener(position: Int) {
        Snackbar.make(binding.root, lectureList[position].scheduleName+"과목을 클릭하였습니다. UI 목적으로 제작한 것으로 대시보드를 통해 개인 강의실로 접근하세요!", Snackbar.LENGTH_SHORT).show()
    }

}