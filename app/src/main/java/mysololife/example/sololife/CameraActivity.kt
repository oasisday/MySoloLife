package mysololife.example.sololife

import android.Manifest.permission.CAMERA
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mysololife.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat


class CameraActivity : AppCompatActivity() {

    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private var lensMode: Int = 1
    private var imageCapture: ImageCapture? = null
    private lateinit var vibrator: Vibrator

    private lateinit var binding: ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        //카메라 권한 허용
        checkCameraPermission()

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        binding.captureBtn.setOnClickListener {
            takePhoto()
        }

        binding.outBtn.setOnClickListener {
            finish()
        }

        binding.flipMode.setOnClickListener {
            flipCamera()
            binding.zoomSeekBar.progress = 0
        }
        binding.zoomBtn.setOnClickListener {
            if (binding.zoomSeekBar.getVisibility() == View.VISIBLE) {
                binding.zoomSeekBar.setVisibility(View.GONE);
            } else {
                binding.zoomSeekBar.setVisibility(View.VISIBLE);
            }
        }
        binding.gridMode.setOnClickListener {
            if (binding.gridLayout.visibility == View.VISIBLE) {
                binding.gridLayout.visibility = View.GONE
            } else {
                binding.gridLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.zoomSeekBar.progress = 0
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder()
                .build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, lensFacing, preview, imageCapture
                )
                val cameraControl = cameraProvider.bindToLifecycle(this,lensFacing,preview,imageCapture)
                binding.zoomSeekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        cameraControl.cameraControl.setLinearZoom(p1/100.toFloat())
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }
                })
            } catch (exc: Exception) {
                Log.e("photo", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
            lensMode = 1
        } else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
            lensMode = 0
        }
        startCamera()
    }
    private fun takePhoto() {
        //진동 효과 주기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    30,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat("yyyy_MM_dd").format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()
        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("photo", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "사진 저장 완료\n경로: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("photo", msg)
                }
            }
        )
    }
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(CAMERA),
            REQUEST_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION -> {
                val resultCode = grantResults.firstOrNull() ?: PackageManager.PERMISSION_GRANTED
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                }
            }
        }
    }
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                CAMERA,
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }

            shouldShowRequestPermissionRationale(CAMERA) -> {
                showCameraPermissionInfoDialog()
            }

            else -> {
                requestCameraPermission()
            }
        }
    }

    private fun showCameraPermissionInfoDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("카메라 권한을 허용하세요")
            setMessage("앱의 무음 카메라 기능을 위해서 권한을 허용해야 합니다.")
            setNegativeButton("거절", null)
            setPositiveButton("동의") { _, _ ->
                requestCameraPermission()
            }
        }.show()
    }

    companion object {
        const val REQUEST_PERMISSION = 100
    }
}