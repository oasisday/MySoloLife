package mysololife.example.sololife.timetable


import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityLectureInitBinding
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class LectureInitActivity : AppCompatActivity() {
    lateinit var binding: ActivityLectureInitBinding
    var lectureDay1: Int = 0
    var lectureDay2: Int = 0
    val cal = Calendar.getInstance()
    private lateinit var infoEntities: List<InfoEntity>
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLectureInitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SpinnerInit()
        binding.colorPickerView.setColorListener(
            ColorEnvelopeListener { envelope, fromUser ->
                binding.colorcode.setText("#" + envelope.getHexCode().toString())
                binding.colorview.setBackgroundColor(envelope.getColor())
            }
        )
        Thread {
            infoEntities = AppDatabase.getInstance(this)?.infoDao()?.getAll() ?: emptyList()
        }.start()

        binding.colorPickerView.attachAlphaSlider(binding.alphaSlideBar)
        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlide)
        binding.saveBtn.setOnClickListener {
            if (blankCheck()) {
                if (timeCheck())
                    add()
            }
        }
    }

    private fun SpinnerInit() {
        binding.timeTypeSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.weekday,
            android.R.layout.simple_list_item_1
        )
        binding.timeTypeSpinner2.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.weekday,
            android.R.layout.simple_list_item_1
        )
        binding.timeTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedValue = parent?.getItemAtPosition(position).toString()
                    // 선택한 값을 기반으로 뷰의 가시성을 조절합니다.
                    if (selectedValue == "x") {
                        binding.startline.visibility = View.VISIBLE
                        lectureDay1 = 0
                    } else {
                        lectureDay1 = position
                        binding.startline.visibility = View.GONE
                        binding.startBtn.setOnClickListener {
                                dayPicker(binding.starttime)
                        }
                        binding.endBtn.setOnClickListener{
                                dayPicker(binding.endtime)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 아무것도 선택되지 않았을 때 필요한 처리를 여기에 추가할 수 있습니다.
                }
            }
        binding.timeTypeSpinner2.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedValue = parent?.getItemAtPosition(position).toString()
                    // 선택한 값을 기반으로 뷰의 가시성을 조절합니다.
                    if (selectedValue == "x") {
                        binding.startBtn2.visibility = View.GONE // 뷰를 숨깁니다.
                        binding.endBtn2.visibility = View.GONE
                        lectureDay2 = 0
                    } else {
                        lectureDay2 = position
                        binding.startBtn2.visibility = View.VISIBLE // 뷰를 숨깁니다.
                        binding.endBtn2.visibility = View.VISIBLE
                        binding.startBtn2.setOnClickListener {
                           dayPicker(binding.starttime2)
                        }
                        binding.endBtn2.setOnClickListener {
                            dayPicker(binding.endtime2)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 아무것도 선택되지 않았을 때 필요한 처리를 여기에 추가할 수 있습니다.
                }
            }
    }

    private fun add() {
        val lectureName: String = binding.lectureEditText.text.toString()
        val roomInfo: String = binding.locationEditText.text.toString()
        val lectureDay: Int = lectureDay1-1
        val lectureDay2: Int = lectureDay2-1
        val startTime: String = binding.starttime.text.toString()
        val endTime: String = binding.endtime.text.toString()
        val startTime2: String = binding.starttime2.text.toString()
        val endTime2: String = binding.endtime2.text.toString()
        val backgroundColor: String = binding.colorcode.text.toString()
        Log.d("roomtest",backgroundColor)
        val textColor = "#ffffff"
        val info1 = InfoEntity(
            lectureName,
            roomInfo,
            lectureDay,
            startTime,
            endTime,
            backgroundColor,
            textColor
        )
        val info2 = InfoEntity(
            lectureName,
            roomInfo,
            lectureDay2,
            startTime2,
            endTime2,
            backgroundColor,
            textColor
        )
        Thread {
            if(lectureDay>=0) AppDatabase.getInstance(this)?.infoDao()?.insert(info1)
            if(lectureDay2>=0) AppDatabase.getInstance(this)?.infoDao()?.insert(info2)
            runOnUiThread {
                Toast.makeText(this, "저장을 완료했습니다.", Toast.LENGTH_SHORT).show()
            }
            finish()
        }.start()
    }

    private fun dayPicker(textview: TextView){
        val timeSetListener =
            OnTimeSetListener { view, hourOfDay, minute ->
                var nextMinute = 0
                nextMinute =
                    if (minute >= 45 && minute <= 59) 0 else if (minute >= 30) 45 else if (minute >= 15) 30 else 15

                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, nextMinute)
                val formattedTime = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(cal.time)
                textview.setText(formattedTime)
            }

        TimePickerDialog(
            this@LectureInitActivity,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }
    private fun timeCheck(): Boolean {
        val flag = true
        val day1: Int = lectureDay1 - 1
        val day2: Int = lectureDay2 - 1
        var startT1 = binding.starttime.text.toString().split(":")
        var endT1 = binding.endtime.text.toString().split(":")
        var startT2 = binding.starttime2.text.toString().split(":")
        var endT2 = binding.endtime2.text.toString().split(":")
        var tmp: Int = 0;

        val sTime1 = startT1[0].toInt() * 60 + startT1[1].toInt()
        val eTime1 = endT1[0].toInt() * 60 + endT1[1].toInt()

        if (sTime1 >= eTime1) {
            Toast.makeText(this@LectureInitActivity, "시간1 설정 오류", Toast.LENGTH_SHORT).show()
            return false
        }

        if (day2 >= 0) {
            val sTime2 = startT2[0].toInt() * 60 + startT2[1].toInt()
            val eTime2 = endT2[0].toInt() * 60 + endT2[1].toInt()
            if (sTime2 >= eTime2) {
                Toast.makeText(this@LectureInitActivity, "시간2 설정 오류", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        for (item in infoEntities) {
            var startT = item.startTime.split(":")
            var endT = item.endTime.split(":")
            val day: Int = item.scheduleDay

            if (day == day1) {
                val sTime = startT[0].toInt() * 60 + startT[1].toInt()
                val eTime = endT[0].toInt() * 60 + endT[1].toInt()

                if (sTime in sTime1 until eTime1) {
                    Toast.makeText(this@LectureInitActivity, "시간 중복", Toast.LENGTH_SHORT).show()
                    return false
                } else if (eTime in sTime1 + 1..eTime1) {
                    Toast.makeText(this@LectureInitActivity, "시간 중복", Toast.LENGTH_SHORT).show()
                    return false
                }

            } else if (day2 >= 0) {
                val sTime2 = startT2[0].toInt() * 60 + startT2[1].toInt()
                val eTime2 = endT2[0].toInt() * 60 + endT2[1].toInt()

                if (day == day2) {
                    val sTime = startT[0].toInt() * 60 + startT[1].toInt()
                    val eTime = endT[0].toInt() * 60 + endT[1].toInt()

                    if (sTime in sTime2 until eTime2) {
                        Toast.makeText(this@LectureInitActivity, "시간 중복", Toast.LENGTH_SHORT).show()
                        return false
                    } else if (eTime in sTime2 + 1..eTime2)
                        return false
                }
            }
        }
        return true
    }


    fun blankCheck(): Boolean {
        if (binding.lectureEditText.text.toString() == "") {
            Toast.makeText(this@LectureInitActivity, "수업명을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.professorEditText.text.toString() == "") {
            Toast.makeText(this@LectureInitActivity, "교수명을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.locationEditText.text.toString() == "") {
            Toast.makeText(this@LectureInitActivity, "장소를 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.colorcode.text.toString() == "#FFFFFFFF") {
            Toast.makeText(this@LectureInitActivity, "색상을 설정해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        if (lectureDay1 - 1 < 0) {
            Toast.makeText(this@LectureInitActivity, "시간을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

