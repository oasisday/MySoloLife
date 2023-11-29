package mysololife.example.sololife.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mysololife.databinding.ActivityLectureInitBinding
import com.example.mysololife.databinding.ActivityLectureMainBinding
import mysololife.example.sololife.alarm.AlarmsetActivity
import mysololife.example.sololife.recorder.RecorderMainActivity

class LectureMainFragment : Fragment() {
    private lateinit var binding: ActivityLectureMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityLectureMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lectureName =   arguments?.getString("lecturename").toString()
        binding.mainlectureName.text = lectureName

        binding.btnStartRecord.setOnClickListener {
            Intent(requireContext(), RecorderMainActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.reserveBtn.setOnClickListener {
            Intent(requireContext(), AlarmsetActivity::class.java).apply {
                putExtra("lecturename", lectureName)
                startActivity(this)
            }
        }
    }

}