package mysololife.example.sololife.recorder

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.mysololife.R

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import java.text.DecimalFormat
import java.text.NumberFormat


class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var btnPlay : ImageButton
    private lateinit var btnBackward: ImageButton
    private lateinit var btnForward : ImageButton
    private lateinit var speedChip : Chip
    private lateinit var seekBar : SeekBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvFilename :TextView
    private lateinit var tvTrackProgress :TextView
    private lateinit var tvTrackDuration :TextView

    private lateinit var runnable: Runnable
    private lateinit var handler : Handler
    private var delay = 10L
    private var jumpValue = 1000

    private var playbackSpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)
        val filePath = intent.getStringExtra("filepath")
        val fileName = intent.getStringExtra("filename")

        toolbar = findViewById(R.id.toolbar)
        tvFilename = findViewById(R.id.tvFilename)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        tvFilename.text = fileName

        tvTrackDuration = findViewById(R.id.tvTrackDuration)
        tvTrackProgress= findViewById(R.id.tvTrackProgress)

        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        btnPlay = findViewById(R.id.btnPlay)
        btnBackward = findViewById(R.id.btnBackward)
        btnForward = findViewById(R.id.btnforward)
        speedChip = findViewById(R.id.chip)
        seekBar= findViewById(R.id.seekbar)

        tvTrackDuration.text = dateFormat(mediaPlayer.duration)

        btnPlay.setOnClickListener{
            playPausePlayer()
        }

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable,delay)
            tvTrackProgress.text = dateFormat(mediaPlayer.currentPosition)
        }

        playPausePlayer()
        seekBar.max = mediaPlayer.duration

        mediaPlayer.setOnCompletionListener {
            btnPlay.background = ResourcesCompat.getDrawable(resources,
                R.drawable.ic_play_circle,theme)
            handler.removeCallbacks(runnable)
        }
        btnForward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition+jumpValue)
            seekBar.progress += jumpValue
        }
        btnBackward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition-jumpValue)
            seekBar.progress -= jumpValue
        }
        speedChip.setOnClickListener{
            if(playbackSpeed != 2f)
                playbackSpeed += 0.25f
            else
                playbackSpeed = 0.5f

            mediaPlayer.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
            speedChip.text = "x $playbackSpeed"
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser)
                    mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?){}

            override fun onStopTrackingTouch(seekBar: SeekBar?){}
        })
    }

    private fun playPausePlayer() {
        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
            btnPlay.background = ResourcesCompat.getDrawable(resources,
                R.drawable.ic_pause_circle,theme)
            handler.postDelayed(runnable,delay)
        }else{
            mediaPlayer.pause()
            btnPlay.background = ResourcesCompat.getDrawable(resources,
                R.drawable.ic_play_circle,theme)
            handler.removeCallbacks(runnable)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }

    private fun dateFormat(duration:Int):String{
        val d= duration/1000
        val s = d%60
        val m = (d/60 % 60)
        val h = ((d - m*60)/3600)
        Log.d("testplease",s.toString()+" "+m.toString()+" "+h.toString())
        val f : NumberFormat = DecimalFormat("00")
        var str = "$m:${f.format(s)}"
        if(h>0)
            str = "$h:$str"
        return str
    }
}