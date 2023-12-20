package mysololife.example.sololife.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityLectureInitBinding
import com.example.mysololife.databinding.ActivityLectureMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mysololife.example.sololife.alarm.AlarmsetActivity
import mysololife.example.sololife.recorder.GalleryActivity
import mysololife.example.sololife.recorder.RecorderMainActivity
import mysololife.example.sololife.timetable.AppDatabase
import java.nio.file.Files.delete

class LectureMainFragment : Fragment() {
    private lateinit var binding: ActivityLectureMainBinding

    val complementaryColors = listOf(
        "#ECF0F1", "#BDC3C7", "#D2D7D3", "#EAEDED", "#F4F6F6",
        "#0000AA", "#0000CC", "#000099", "#3964E5", "#FF8C00",
        "#4040FF", "#3333CC", "#262699", "#0000AA", "#FFD700",
        "#8080FF", "#6666CC", "#4D4C99", "#7CFC00", "#FF4500",
        "#BFBFFF", "#9999CC", "#737399", "#3EC5F1", "#00Bfff",
        "#B9E0FD", "#7ECBEB", "#85E0FF", "#B6dbf8", "#a7cde5",
        "#9fc9e4", "#aed7ea", "#cee4dd", "#a3cde9", "#c0d8ea",
        "#ECF0F1", "#BDC3C7", "#D2D7D3", "#EAEDED", "#F4F6F6",
        "#BFBFFF", "#9999CC", "#737399", "#B9E0FD", "#7ECBEB",
        "#85E0FF", "#B6dbf8", "#a7cde5", "#9fc9e4", "#aed7ea",
    )

    val cloudColorsExtended =  complementaryColors
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

        binding.btnRecordrepository.setOnClickListener {
            Intent(requireContext(), GalleryActivity::class.java).apply {
                putExtra("lecturename",lectureName)
                startActivity(this)
            }
        }
        binding.locationShareBtn.setOnClickListener {
            val bundle = bundleOf("alarmlecturename" to lectureName)
            view?.findNavController()?.navigate(R.id.action_lectureMainFragment_to_alarmsetFragment,bundle)
        }
        binding.deleteStudyRoomBtn.setOnClickListener {
            GlobalScope.launch {
                delete(lectureName)
            }
            Toast.makeText(requireContext(),"${lectureName}을 삭제하였습니다.",Toast.LENGTH_SHORT).show()
            view?.findNavController()?.navigate(R.id.action_lectureMainFragment_to_homeFragment)
        }
        binding.colorChangeBtn.setOnClickListener {
            binding.colorChangeBtn.setOnClickListener {
                val randomExtendedCloudColor = generateRandomExtendedCloudColor()
                Toast.makeText(requireContext(), "${lectureName} 과목의 색상을 구름색으로 교체했어요!", Toast.LENGTH_SHORT).show()

                // 애니메이션에 사용할 새로운 뷰 생성
                val animatedView = View(requireContext())
                animatedView.setBackgroundColor(Color.parseColor(randomExtendedCloudColor))
                animatedView.alpha = 0f

                // 뷰를 레이아웃에 추가
                val rootView = activity?.findViewById<ViewGroup>(android.R.id.content)
                rootView?.addView(
                    animatedView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // 투명도 애니메이션 적용
                val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
                alphaAnimator.addUpdateListener { animation ->
                    val alpha = animation.animatedValue as Float
                    animatedView.alpha = alpha
                }
                alphaAnimator.duration = 800 // 애니메이션 지속 시간 (0.5초)
                alphaAnimator.interpolator = AccelerateInterpolator()

                // 크기 애니메이션 적용
                val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    animatedView,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.1f)
                )
                scaleAnimator.duration = 800 // 애니메이션 지속 시간 (0.5초)
                scaleAnimator.interpolator = AccelerateInterpolator()

                // 애니메이션 진행 조절
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(alphaAnimator, scaleAnimator)

                // 애니메이션 시작
                animatorSet.start()

                // 애니메이션 종료 후 뷰 제거
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        rootView?.removeView(animatedView)
                        // 이후 다른 동작 수행
                    }
                })

                // 데이터베이스 업데이트
                Thread {
                    AppDatabase.getInstance(requireContext())?.infoDao()?.updateColor(lectureName, randomExtendedCloudColor)
                }.start()
            }
        }

    }
    private fun delete(lecturename: String) {
        Thread {
            AppDatabase.getInstance(requireContext())?.infoDao()?.deleteByScheduleName(lecturename)
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "과목 삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
    fun generateRandomExtendedCloudColor(): String {
        return cloudColorsExtended.random()
    }

}