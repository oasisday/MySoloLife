package mysololife.example.sololife

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.media.AudioManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityCameraBinding
import com.jraska.falcon.Falcon
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import io.fotoapparat.selector.off
import io.fotoapparat.selector.torch
import io.fotoapparat.view.CameraView
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {
    val permissions =
        arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES)
    lateinit var binding: ActivityCameraBinding
    var fotoapparat: Fotoapparat? = null
    val filename = "test.png"
    val sd = Environment.getExternalStorageDirectory()
    val dest = File(sd, filename)
    var fotoapparatState: FotoapparatState? = null
    var cameraStatus: CameraState? = null
    var flashState: FlashState? = null

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null

    private val requestScreenCapturePermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val mediaProjection =mediaProjectionManager?.getMediaProjection(result.resultCode,
                intent!!
            )
            // MediaProjection을 사용하여 화면 캡처를 진행할 수 있습니다.
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        createFotoapparat()

        cameraStatus = CameraState.BACK
        flashState = FlashState.OFF
        fotoapparatState = FotoapparatState.OFF

        binding.fabCamera.setOnClickListener {
            val file = File(getExternalFilesDir(null), "screenshot.png")
            //Falcon.takeScreenshot(this, file)
           // Log.d("cameraTest",file.toString())
            val container = getWindow().getDecorView()
            container.buildDrawingCache()
            val captureView = container.getDrawingCache()
            saveBitmapToFile(captureView)
        }

        binding.fabSwitchCamera.setOnClickListener {
            switchCamera()
        }
        binding.fabFlash.setOnClickListener {
            changeFlashState()
        }
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // 사용자로부터 권한을 요청하고 화면 캡처를 시작하는 코드
        requestScreenCapturePermission.launch(mediaProjectionManager?.createScreenCaptureIntent())
    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_MEDIA_IMAGES
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    private fun createFotoapparat() {
        val cameraView = findViewById<CameraView>(R.id.camera_view)

        fotoapparat = Fotoapparat(
            context = this,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back(),
            logger = loggers(
                logcat()
            ),
            cameraErrorCallback = { error ->
                println("Recorder errors: $error")
            }
        )
    }

    override fun onStart() {
        super.onStart()
        if (hasNoPermissions()) {
            requestPermission()
        } else {
            fotoapparat?.start()
            fotoapparatState = FotoapparatState.ON
        }
    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
        FotoapparatState.OFF
    }

    override fun onResume() {
        super.onResume()
        if (!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF) {
            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun takePhoto() {
        if (hasNoPermissions()) {
            requestPermission()
        } else {
            val mgr = getSystemService(AUDIO_SERVICE) as AudioManager
            fotoapparat
                ?.takePicture()
                ?.saveToFile(dest)
        }
    }

    private fun switchCamera() {
        fotoapparat?.switchTo(
            lensPosition = if (cameraStatus == CameraState.BACK) front() else back(),
            cameraConfiguration = CameraConfiguration()
        )

        if (cameraStatus == CameraState.BACK) cameraStatus = CameraState.FRONT
        else cameraStatus = CameraState.BACK
    }

    private fun changeFlashState() {
        fotoapparat?.updateConfiguration(
            CameraConfiguration(
                flashMode = if(flashState == FlashState.TORCH) off() else torch()
            )
        )

        if(flashState == FlashState.TORCH) flashState = FlashState.OFF
        else flashState = FlashState.TORCH
    }

    private fun captureScreen() {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val bitmap = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            metrics.densityDpi,
            VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            null,
            null,
            null
        )?.surface?.let { surface ->
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            val srcRect = Rect(0, 0, width, height)
            canvas.drawBitmap(bitmap, srcRect, srcRect, paint)
            bitmap
        }

        if (bitmap != null) {
            // 캡처된 이미지(bitmap)를 사용하여 필요한 동작을 수행
            saveBitmapToFile(bitmap)
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap) {
        val file = File(getExternalFilesDir(null), "screenshot.png")
        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            Log.d("cameratest",file.toString())
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    enum class CameraState {
        FRONT, BACK
    }

    enum class FlashState {
        TORCH, OFF
    }

    enum class FotoapparatState {
        ON, OFF
    }
}