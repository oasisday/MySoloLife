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
        binding.colorPickerView.attachAlphaSlider(binding.alphaSlideBar)
        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlide)
        binding.saveBtn.setOnClickListener { add()
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
                            binding.startBtn.setOnClickListener {
                                dayPicker(binding.starttime)
                            }
                        }
                        binding.endBtn.setOnClickListener {
                            binding.endBtn.setOnClickListener {
                                dayPicker(binding.endtime)
                            }
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

    private fun dayPicker(textview: TextView) : OnTimeSetListener {
        val timeSetListener =
            OnTimeSetListener { view, hourOfDay, minute ->
                val adjustedMinute = (minute / 5) * 5 // 5의 배수로 조정
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, adjustedMinute)
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
        return timeSetListener
    }
}

