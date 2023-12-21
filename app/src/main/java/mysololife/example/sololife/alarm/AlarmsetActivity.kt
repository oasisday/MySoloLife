package mysololife.example.sololife.alarm

import android.Manifest
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityAlarmsetBinding
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import de.coldtea.smplr.smplralarm.alarmNotification
import de.coldtea.smplr.smplralarm.apis.SmplrAlarmAPI
import de.coldtea.smplr.smplralarm.apis.SmplrAlarmListRequestAPI
import de.coldtea.smplr.smplralarm.channel
import de.coldtea.smplr.smplralarm.smplrAlarmChangeOrRequestListener
import de.coldtea.smplr.smplralarm.smplrAlarmRenewMissingAlarms
import de.coldtea.smplr.smplralarm.smplrAlarmSet
import mysololife.example.sololife.MainActivity
import mysololife.example.sololife.recorder.AudioService
import mysololife.example.sololife.recorder.RecorderMainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmsetActivity : AppCompatActivity() {
    private lateinit var smplrAlarmListRequestAPI: SmplrAlarmListRequestAPI
    private lateinit var binding : ActivityAlarmsetBinding
    private val clickedDays = mutableSetOf<String>()
    private lateinit var alarmlistAdapter : AlarmListAdapter
    private lateinit var tempalarmList : MutableList<AlarmItem>
    private val clickDays = mutableListOf<String>()
    lateinit var lecture :String
    val cal = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smplrAlarmListRequestAPI = smplrAlarmChangeOrRequestListener(applicationContext) { jsonString ->
            updateListViewWithAlarmData(jsonString)
            Log.d("alarmTest",jsonString)
        }
        lecture = intent.getStringExtra("lecturename").toString()

        binding = ActivityAlarmsetBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }


        val fullScreenIntent = Intent(
            applicationContext,
            RecorderMainActivity::class.java
        )
        fullScreenIntent.putExtra(SmplrAlarmAPI.SMPLR_ALARM_REQUEST_ID,lecture)


        val isTiramisuOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

        var hasNotificationPermission =
            if (isTiramisuOrHigher)
                ContextCompat.checkSelfPermission(
                    this,
                    notificationPermission
                ) == PackageManager.PERMISSION_GRANTED
            else true
        val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            hasNotificationPermission = it
        }

        if (!hasNotificationPermission) {
            launcher.launch(notificationPermission)
        }

        smplrAlarmListRequestAPI.requestAlarmList()

        val formattedTime = SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(cal.time)
        binding.timePicker.text = formattedTime

        val toggleButtons = listOf(
            binding.mondayToggleButton,
            binding.tuesdayToggleButton,
            binding.wednesdayToggleButton,
            binding.thursdayToggleButton,
            binding.fridayToggleButton,
            binding.saturdayToggleButton
        )

        toggleButtons.forEach { button ->
            button.apply {
                isChecked = false

                setOnCheckedChangeListener { _, isChecked ->
                    val day = button.text.toString()

                    if (isChecked) {
                        clickedDays.add(day)
                    } else {
                        clickedDays.remove(day)
                    }
                    Log.d("ClickedDays", "Clicked Days: $clickedDays")
                }
            }
        }


        binding.timePicker.setOnClickListener {
            dayPicker()
        }
        binding.plusTimeTextView.setOnClickListener {
            binding.alarmsetConstraintLayout.isVisible = true
            binding.bottomSheetBG.isVisible = true
            clickedDays.clear()
        }

        binding.btnCancel.setOnClickListener {
            binding.alarmsetConstraintLayout.isVisible = false
            binding.bottomSheetBG.isVisible = false
        }

        binding.btnOk.setOnClickListener {
            val toggleBtn = listOf(
                binding.mondayToggleButton,
                binding.tuesdayToggleButton,
                binding.wednesdayToggleButton,
                binding.thursdayToggleButton,
                binding.fridayToggleButton,
                binding.saturdayToggleButton
            )

            clickDays.clear()
            binding.alarmsetConstraintLayout.isVisible = false
            binding.bottomSheetBG.isVisible = false
            val time = binding.timePicker.text
            val h = time.substring(0, 2).toInt()
            val m = time.substring(3, 5).toInt()
            smplrAlarmSet(applicationContext) {
                hour { h }
                min { m }
                weekdays {
                    if (binding.mondayToggleButton.isChecked) {
                        monday()
                        clickDays.add("MONDAY")
                    }
                    if (binding.tuesdayToggleButton.isChecked) {
                        tuesday()
                        clickDays.add("TUESDAY")
                    }
                    if (binding.wednesdayToggleButton.isChecked) {
                        wednesday()
                        clickDays.add("WEDNESDAY")
                    }
                    if (binding.thursdayToggleButton.isChecked) {
                        thursday()
                        clickDays.add("THURSDAY")
                    }
                    if (binding.fridayToggleButton.isChecked) {
                        friday()
                        clickDays.add("FRIDAY")
                    }
                    if (binding.saturdayToggleButton.isChecked) {
                        saturday()
                        clickDays.add("SATURDAY")
                    }
                }
                receiverIntent { fullScreenIntent }
                notification {
                    alarmNotification {
                        firstButtonText { "Snooze" }
                        secondButtonText { "Dismiss" }
                        smallIcon { R.drawable.round_keyboard_voice_24 }
                        title { "$lecture 녹음" }
                        message { "$lecture 시작 시간입니다. 알림을 클릭하세요! " }
                        bigText { "알림 클릭시 ${lecture}의 포그라운드 녹음을 시작합니다." }
                        autoCancel { true }
                    }
                }
                notificationChannel {
                    channel {
                        importance { NotificationManager.IMPORTANCE_HIGH }
                        showBadge { false }
                        name { "de.coldtea.smplr.alarm.channel" }
                        description { "간단한 예약 하기" }
                    }
                }
                Toast.makeText(this@AlarmsetActivity, "녹음 예약을 설정했습니다.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }
    private fun dayPicker(){
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                val formattedTime = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(cal.time)
                binding.timePicker.text = formattedTime
            }


        TimePickerDialog(
            this,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun updateListViewWithAlarmData(jsonString: String) {
        val alarmList = parseJsonToAlarmList(jsonString)
        for (alarmItem in alarmList) {
            Log.d("AlarmItem", "RequestId: ${alarmItem.requestId}, Hour: ${alarmItem.hour}, Minute: ${alarmItem.minute}, Weekdays: ${alarmItem.weekDays}, IsActive: ${alarmItem.isActive}")
            val sharedPrefs = getSharedPreferences(RECORD_NAME, Context.MODE_PRIVATE)
            if (!sharedPrefs.contains(alarmItem.requestId.toString())) {
                with(sharedPrefs.edit()) {
                    putString(alarmItem.requestId.toString(), lecture)
                    Log.d("AlarmItem1",alarmItem.requestId.toString() +lecture)
                    apply()
                }
            }
        }
        updateListView(alarmList.toMutableList())
    }

    fun parseJsonToAlarmList(jsonString: String): List<AlarmItem> {
        val gson = Gson()
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val alarmItemsArray = jsonObject.getAsJsonArray("alarmItems")

        val alarmItemListType = object : TypeToken<List<AlarmItem>>() {}.type
        return gson.fromJson(alarmItemsArray, alarmItemListType)
    }

    // 6. ListView를 업데이트하는 함수를 구현합니다.
    fun updateListView(alarmList: MutableList<AlarmItem>) {
        tempalarmList = alarmList
        runOnUiThread {
            try {
                alarmlistAdapter = AlarmListAdapter(this@AlarmsetActivity, alarmList)
                binding.alarmListView.adapter = alarmlistAdapter
            } catch (e: Exception) {
                Log.e("alarmerror", e.toString())
            }
        }
    }
}