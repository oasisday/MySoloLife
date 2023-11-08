package mysololife.example.sololife.dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMainDashboardBinding
import mysololife.example.sololife.recorder.RecorderMainActivity
import mysololife.example.sololife.timetable.AppDatabase
import mysololife.example.sololife.timetable.InfoEntity

class MainDashboardActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityMainDashboardBinding
    private var lectureList = mutableListOf<InfoEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainDashboardBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        Thread {
            val infoEntities =
                AppDatabase.getInstance(this)?.infoDao()?.getAllLecture() ?: emptyList()
            lectureList.addAll(infoEntities)
        }.start()

        binding.recyclerView.apply {
            this.adapter = DashboardAdapter(lectureList, this@MainDashboardActivity)
            layoutManager = GridLayoutManager(this@MainDashboardActivity, 2)
        }
        binding.recyclerView.adapter!!.apply {
            notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        if(lectureList.isEmpty()){
            binding.animationView.visibility = View.VISIBLE
            binding.animationView.setAnimation(R.raw.heart)
            val heartEmoji = "\uD83D\uDC97"
            binding.textView3.text = "강의를 등록하세요 $heartEmoji"
            binding.textView3.textSize =16f
        }
        else {
            binding.animationView.visibility = View.VISIBLE
        }
    }

    override fun onItemClickListener(position: Int) {
        Intent(this,LectureMainActivity::class.java).apply {
            putExtra("lecturename",lectureList[position].scheduleName)
            startActivity(this)
        }
    }
}