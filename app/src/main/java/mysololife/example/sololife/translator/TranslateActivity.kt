package mysololife.example.sololife.translator

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityTranslateBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class TranslateActivity : AppCompatActivity() {
    lateinit var binding: ActivityTranslateBinding
    private var items= arrayOf("ENGLISH","KOREAN","JAPANESE","CHINESE","SPANISH","GERMAN", "FRENCH")
    private var conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    private var imageBitmap: Bitmap? = null
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // The image capture was successful, handle the result
                val data: Intent? = result.data
                // Process the captured image data

                val extras = data!!.extras
                imageBitmap = extras!!.get("data") as Bitmap
                binding.screen!!.setImageBitmap(imageBitmap)

            } else {
                // The image capture was canceled or failed
                // Handle accordingly
            }
        }

    var intentActivityResultLauncher:ActivityResultLauncher<Intent>?=null
    lateinit var inputImage: InputImage
    lateinit var textRecognizer: TextRecognizer
    private val STORAGE_PERMISSION_CODE=113

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        binding= DataBindingUtil.setContentView(this,R.layout.activity_translate)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        intentActivityResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                val data=it.data
                val imageUri=data?.data

                convertImageToText(imageUri)
            }
        )

        binding.galBtn.setOnClickListener{
            val chooseIntent = Intent()
            chooseIntent.type="image/*"
            chooseIntent.action=Intent.ACTION_GET_CONTENT
            intentActivityResultLauncher?.launch((chooseIntent))
        }

        val itemsAdapter:ArrayAdapter<String> =ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, items)

        binding.languageFrom.setAdapter(itemsAdapter)

        binding.languageTo.setAdapter(itemsAdapter)

        binding.translate.setOnClickListener {

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(selectFrom())
                .setTargetLanguage(selectTo())
                .build()

            val translator = Translation.getClient(options)

            binding.progressBar.visibility = View.VISIBLE
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {

                    binding.progressBar.visibility = View.INVISIBLE

                    translator.translate(binding.input.text.toString())
                        .addOnSuccessListener { translatedText ->

                            binding.output.text=translatedText

                        }
                        .addOnFailureListener { exception ->

                            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()

                        }

                }
                .addOnFailureListener { exception ->

                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()

                }


        }


        binding.snapBtn.setOnClickListener(View.OnClickListener {
            dispatchTakePictureIntent()
        })

        binding.passBtn.setOnClickListener(View.OnClickListener {
            detectText()
        })
    }

    private fun convertImageToText(imageUri: Uri?) {
        try {
            inputImage = InputImage.fromFilePath(applicationContext, imageUri!!)

            val result: Task<Text> = textRecognizer.process(inputImage)
                .addOnSuccessListener{
                    binding.input.setText(it.text)
                }.addOnFailureListener{
                    binding.input.setText("Error" + it.message)
                }
        }catch (e:Exception){

        }
    }

    override fun onResume() {
        super.onResume()
        //checkPermission(READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)

    }

    private fun checkPermission(permission:String, requestCode:Int) {
        if (ContextCompat.checkSelfPermission(this@TranslateActivity, permission) == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this@TranslateActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@TranslateActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            }else{
                //Toast.makeText(this@TranslateActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == 1000) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) { //거부
                Toast.makeText(this@TranslateActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun detectText() {
        val image = imageBitmap?.let { InputImage.fromBitmap(it, 0) }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        if (image != null) {
            recognizer.process(image).addOnSuccessListener { visionText ->
                val blocks = visionText.textBlocks
                if(blocks.size==0){
                    Toast.makeText(this, "No Texts Found", Toast.LENGTH_LONG).show()
                }
                else
                {
                    for(block in visionText.textBlocks)
                    {
                        val txt = block.getText()
                        binding.input.setText(txt)

                    }
                }
            }
        }
        else
        {
            Toast.makeText(this, "Photo needs to be taken", Toast.LENGTH_SHORT).show()
        }
    }


    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {

        val cameraPermissionCheck = ContextCompat.checkSelfPermission(
            this@TranslateActivity,
            android.Manifest.permission.CAMERA
        )
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) { // 권한이 없는 경우
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                1000
            )
        }else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                takePictureLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun selectFrom(): String {

        return when(binding.languageFrom.text.toString()){

            ""-> TranslateLanguage.ENGLISH

            "ENGLISH"->TranslateLanguage.ENGLISH

            "KOREAN"->TranslateLanguage.KOREAN

            "JAPANESE"->TranslateLanguage.JAPANESE

            "CHINESE"->TranslateLanguage.CHINESE

            "SPANISH"->TranslateLanguage.SPANISH

            "GERMAN"->TranslateLanguage.GERMAN

            "FRENCH"->TranslateLanguage.FRENCH

            else->TranslateLanguage.ENGLISH

        }


    }

    private fun selectTo(): String {

        return when(binding.languageTo.text.toString()){

            ""-> TranslateLanguage.KOREAN

            "ENGLISH"->TranslateLanguage.ENGLISH

            "KOREAN"->TranslateLanguage.KOREAN

            "JAPANESE"->TranslateLanguage.JAPANESE

            "CHINESE"->TranslateLanguage.CHINESE

            "SPANISH"->TranslateLanguage.SPANISH

            "GERMAN"->TranslateLanguage.GERMAN

            "FRENCH"->TranslateLanguage.FRENCH

            else->TranslateLanguage.KOREAN

        }


    }

}