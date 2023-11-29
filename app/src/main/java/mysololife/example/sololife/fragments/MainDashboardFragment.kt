package mysololife.example.sololife.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMainDashboardBinding

import mysololife.example.sololife.dashboard.DashboardAdapter
import mysololife.example.sololife.dashboard.LectureMainActivity
import mysololife.example.sololife.dashboard.OnItemClickListener
import mysololife.example.sololife.timetable.AppDatabase
import mysololife.example.sololife.timetable.InfoEntity
import mysololife.example.sololife.timetable.LectureInitActivity

class MainDashboardFragment : Fragment(), OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityMainDashboardBinding
    private var lectureList = mutableListOf<InfoEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMainDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    private fun initializeRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = DashboardAdapter(lectureList, this)
        recyclerView.adapter?.notifyDataSetChanged()

        if (lectureList.isEmpty()) {
            binding.animationView.visibility = View.VISIBLE
            binding.animationView.setAnimation(R.raw.nofile_animation)
            binding.animationView.playAnimation();
            binding.textView3.text = "아이콘을 눌러 강의를 등록하세요"
            binding.textView3.textSize = 16f
            binding.animationView.setOnClickListener {
                view?.findNavController()?.navigate(R.id.action_maindashboardFragment_to_lectureInitFragment)
            }
        } else {
            binding.animationView.visibility = View.GONE
            binding.textView3.text = "과목"
            binding.textView3.textSize = 32f
        }
    }

    override fun onResume() {
        super.onResume()
        lectureList.clear()
        Thread {
            val infoEntities =
                AppDatabase.getInstance(requireContext())?.infoDao()?.getAllLecture() ?: emptyList()
            lectureList.addAll(infoEntities)

            activity?.runOnUiThread {
                initializeRecyclerView()
            }
        }.start()
    }
    override fun onItemClickListener(position: Int) {
        val bundle = bundleOf("lecturename" to lectureList[position].scheduleName)
        view?.findNavController()?.navigate(R.id.action_maindashboardFragment_to_lectureMainFragment,bundle)
    }
}

