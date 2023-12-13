package mysololife.example.sololife.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mysololife.databinding.FragmentTimetableBinding
import com.islandparadise14.mintable.model.ScheduleEntity
import com.islandparadise14.mintable.tableinterface.OnScheduleClickListener
import com.islandparadise14.mintable.tableinterface.OnScheduleLongClickListener
import mysololife.example.sololife.dashboard.LectureMainActivity
import mysololife.example.sololife.timetable.AppDatabase
import mysololife.example.sololife.timetable.LectureInitActivity
import mysololife.example.sololife.timetable.toScheduleEntity

class TimeTableFragment : Fragment() {
    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    private val day = arrayOf("월", "화", "수", "목", "금", "토")
    private val scheduleList: ArrayList<ScheduleEntity> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.table.setOnScheduleClickListener(object : OnScheduleClickListener {
            override fun scheduleClicked(entity: ScheduleEntity) {
                Intent(requireContext(), LectureMainActivity::class.java).apply {
                    putExtra("lecturename", entity.scheduleName)
                    startActivity(this)
                }
            }
        })

        binding.table.setOnScheduleLongClickListener(object : OnScheduleLongClickListener {
            override fun scheduleLongClicked(entity: ScheduleEntity) {
                delete(entity)
                scheduleList.removeIf { it.scheduleName == entity.scheduleName }
                requireActivity().runOnUiThread {
                    binding.table.initTable(day)
                    binding.table.updateSchedules(scheduleList)
                }
            }
        })

//        binding.lectureAdd.setOnClickListener {
//            Intent(requireContext(), LectureInitActivity::class.java).apply {
//                startActivity(this)
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        Thread {
            val infoEntities = AppDatabase.getInstance(requireContext())?.infoDao()?.getAll()
                ?: emptyList()
            val scheduleEntities = infoEntities.map { it.toScheduleEntity() }
            scheduleList.addAll(scheduleEntities)
        }.start()
        binding.table.updateSchedules(scheduleList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun delete(entity: ScheduleEntity) {
        Thread {
            entity?.let { word ->
                AppDatabase.getInstance(requireContext())?.infoDao()?.deleteByScheduleName(word.scheduleName)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "과목 삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}