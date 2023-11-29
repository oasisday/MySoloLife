package mysololife.example.sololife.dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityLectureInitBinding
import com.example.mysololife.databinding.ActivityLectureMainBinding
import mysololife.example.sololife.alarm.AlarmsetActivity
import mysololife.example.sololife.recorder.RecorderMainActivity

class LectureMainActivity : AppCompatActivity() {
    lateinit var binding: ActivityLectureMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLectureMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lectureName = intent.getStringExtra("lecturename")

        binding.mainlectureName.text = lectureName
        binding.btnRecordrepository.setOnClickListener {
            Intent(this,RecorderMainActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.reserveBtn.setOnClickListener {
            Intent(this,AlarmsetActivity::class.java).apply {
                putExtra("lecturename",lectureName)
                startActivity(this)
            }
        }
    }
}