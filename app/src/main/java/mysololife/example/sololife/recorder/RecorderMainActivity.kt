package mysololife.example.sololife.recorder

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings.Global
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityRecorderMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.play.core.integrity.p
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mysololife.example.sololife.recorder.AppDatabase
import mysololife.example.sololife.recorder.AudioRecord
import mysololife.example.sololife.recorder.GalleryActivity
import mysololife.example.sololife.recorder.OnTimerTickListener
import mysololife.example.sololife.recorder.Timer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutput
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date


const val REQUEST_CODE = 200

class RecorderMainActivity : AppCompatActivity(), OnTimerTickListener {
    private lateinit var amplitudes: ArrayList<Float>
    private var permissions = arrayOf(RECORD_AUDIO)
    private var permissionGranted = false
    private lateinit var binding: ActivityRecorderMainBinding
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false
    private var duration = ""
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var vibrator: Vibrator
    private lateinit var timer: Timer
    private lateinit var btnCancel: Button
    private lateinit var btnOk: Button
    private lateinit var filenameInput: EditText
    private lateinit var recorder: MediaRecorder
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecorderMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        permissionGranted = (ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED )

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }
            //초기화 해주기
            val bottomSheet = findViewById<LinearLayout>(R.id.bottomSheet)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = 0
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            btnCancel = findViewById<Button>(R.id.btnCancel)
            btnOk = findViewById<Button>(R.id.btnOk)

            filenameInput = findViewById<EditText>(R.id.filenameInput)
            timer = Timer(this)
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            db = Room.databaseBuilder(
                this, AppDatabase::class.java, "audioRecords"
            ).build()

            binding.btnRecord.setOnClickListener {
                when {
                    isPaused -> resumeRecorder()
                    isPaused -> resumeRecorder()
                    isRecording -> pauseRecorder()
                    else -> startRecording()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            50,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                }
            }
            binding.btnList.setOnClickListener {
                startActivity(Intent(this, GalleryActivity::class.java))
            }
            binding.btnDone.setOnClickListener {
                stopRecorder()
                Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show()

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.bottomSheetBG.visibility = View.VISIBLE
                filenameInput.setText(filename)
            }


            //bottom sheet view 버튼 설정
            btnCancel.setOnClickListener {
                File("$dirPath$filename.mp3").delete()
                dismiss()
            }

            btnOk.setOnClickListener {
                dismiss()
                save()
            }

            binding.btnDelete.setOnClickListener {
                stopRecorder()
                File("$dirPath$filename.mp3")
                Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
            }
            binding.btnDelete.isClickable = false
    }

    private fun save() {
        val newFilename = filenameInput.text.toString()
        if (newFilename != filename) {
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }

        var filePath = "$dirPath$newFilename.mp3"
        var timestamp = Date().time
        var ampsPath = "$dirPath$newFilename"

        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        } catch (e: IOException) {
        }

        var record = AudioRecord(newFilename, filePath, timestamp, duration, ampsPath)

        GlobalScope.launch {
            db.audioRecorDao().insert(record)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun pauseRecorder() {
        recorder.pause()
        isPaused = true
        binding.btnRecord.setImageResource(R.drawable.baseline_record)

        timer.pause()
    }

    private fun resumeRecorder() {
        recorder.resume()
        isPaused = false
        binding.btnRecord.setImageResource(R.drawable.baseline_pause_24)

        timer.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            permissionGranted = (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
            recorder = MediaRecorder()
            dirPath = "${externalCacheDir?.absolutePath}/"
            var simpleDateFormat = SimpleDateFormat("yyyy.MM.dd_hh시 mm분")
            var date = simpleDateFormat.format(Date())
            filename = "audio_record_$date"

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile("$dirPath$filename.mp3")
                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e("RecordTest",e.toString())
                }
                start()

            }

            binding.btnRecord.setImageResource(R.drawable.baseline_pause_24)
            isRecording = true
            isPaused = false

            timer.start()

            binding.btnDelete.isClickable = true
            binding.btnDelete.setImageResource(R.drawable.baseline_clear_24)

            binding.btnList.visibility = View.GONE
            binding.btnDone.visibility = View.VISIBLE
    }


    private fun dismiss() {
        binding.bottomSheetBG.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        hideKeyboard(filenameInput)
    }

    private fun stopRecorder() {
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused = false
        isRecording = false

        binding.btnList.visibility = View.VISIBLE
        binding.btnDone.visibility = View.GONE

        binding.btnDelete.isClickable = false
        binding.btnDelete.setImageResource(R.drawable.baseline_clear_disabled_24)

        binding.btnRecord.setImageResource(R.drawable.baseline_record)
        binding.tvTimer.text = "00:00:00"
        binding.waveformView.clear()
        amplitudes = binding.waveformView.clear()
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
        this.duration = duration.dropLast(3)
        binding.waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}