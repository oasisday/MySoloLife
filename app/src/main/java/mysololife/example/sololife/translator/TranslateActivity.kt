package mysololife.example.sololife.translator

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class TranslateActivity : AppCompatActivity() {
    lateinit var binding: ActivityTranslateBinding
    //todo 나머지 언어 오류남
    private var items1= arrayOf("탐지모드","영어","한국어")
    private var items2= arrayOf("영어","한국어")
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

    val languageIdentifier = LanguageIdentification.getClient()

    var intentActivityResultLauncher:ActivityResultLauncher<Intent>?=null
    lateinit var inputImage: InputImage
    lateinit var textRecognizer: TextRecognizer
    lateinit var ktextRecognizer: TextRecognizer
    lateinit var jtextRecognizer: TextRecognizer
    lateinit var ctextRecognizer: TextRecognizer
    private val STORAGE_PERMISSION_CODE=113

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        binding= DataBindingUtil.setContentView(this,R.layout.activity_translate)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        ktextRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        jtextRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
        ctextRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

        intentActivityResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                val data=it.data
                val imageUri=data?.data

                convertImageToText(imageUri)
            }
        )

        binding.outbtn.setOnClickListener {
            finish()
        }
        binding.galBtn.setOnClickListener{
            val chooseIntent = Intent()
            chooseIntent.type="image/*"
            chooseIntent.action=Intent.ACTION_GET_CONTENT
            intentActivityResultLauncher?.launch((chooseIntent))
        }

        val itemsAdapter1:ArrayAdapter<String> =ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, items1)
        val itemsAdapter2:ArrayAdapter<String> =ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, items2)

        binding.languageFrom.setAdapter(itemsAdapter1)

        binding.languageTo.setAdapter(itemsAdapter2)

        binding.translate.setOnClickListener {

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(selectFrom())
                .setTargetLanguage(selectTo())
                .build()

            Log.d("sibar",selectFrom())
            Log.d("sibar",selectTo())
            val translator = Translation.getClient(options)

            binding.progressBar.visibility = View.VISIBLE
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {

                    binding.progressBar.visibility = View.INVISIBLE

                    translator.translate(binding.input.text.toString())
                        .addOnSuccessListener { translatedText ->

                            binding.output.text=translatedText
                            binding.input.text.clear()
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
                    Toast.makeText(this, "영어가 감지되었습니다.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{
                    //binding.input.setText("Error" + it.message)
                    val result: Task<Text> = ktextRecognizer.process(inputImage)
                        .addOnSuccessListener{
                            Toast.makeText(this, "한국어가 감지되었습니다", Toast.LENGTH_SHORT).show()
                            binding.input.setText(it.text)
                        }.addOnFailureListener{
                            //binding.input.setText("Error" + it.message)
                            val result: Task<Text> = jtextRecognizer.process(inputImage)
                                .addOnSuccessListener{
                                    binding.input.setText(it.text)
                                    Toast.makeText(this, "일본어가 감지되었습니다.", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener{
                                    binding.input.setText("Error" + it.message)
                                    val result: Task<Text> = ctextRecognizer.process(inputImage)
                                        .addOnSuccessListener{
                                            binding.input.setText(it.text)
                                            Toast.makeText(this, "Chinese Recognized", Toast.LENGTH_SHORT).show()

                                        }.addOnFailureListener{
                                            binding.input.setText("Error" + it.message)

                                        }
                                }
                        }
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
                    Toast.makeText(this, "텍스트가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "해당 기능을 위해서 사진을 먼저 찍어야 합니다.", Toast.LENGTH_SHORT).show()
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
        var txt = binding.languageFrom.text.toString()
        var lng: String = identifyLang(binding.input.text.toString())
        Log.d("sibar",txt)
        if(txt=="탐지모드")
        {
            var from ="en"
            languageIdentifier.identifyLanguage(lng)
                .addOnSuccessListener { languageCode ->
                    if (languageCode == "und") {
                        from="en"
                    } else {
                        from=languageCode
                    }

                }
                .addOnFailureListener {
                    // Model couldn’t be loaded or other internal error.
                    // ...
                    from="en"
                }
            return from
        }

        return when(txt){

            ""-> TranslateLanguage.ENGLISH

            "영어"->TranslateLanguage.ENGLISH

            "한국어"->TranslateLanguage.KOREAN

            "일본어"->TranslateLanguage.JAPANESE

            "중국어"->TranslateLanguage.CHINESE

            "스페인어"->TranslateLanguage.SPANISH

            "독일어"->TranslateLanguage.GERMAN

            "불어"->TranslateLanguage.FRENCH

            else->TranslateLanguage.ENGLISH

        }


    }

    private fun selectTo(): String {

        return when(binding.languageTo.text.toString()){

            ""-> TranslateLanguage.KOREAN

            "영어"->TranslateLanguage.ENGLISH

            "한국어"->TranslateLanguage.KOREAN

            "일본어"->TranslateLanguage.JAPANESE

            "중국어"->TranslateLanguage.CHINESE

            "스페인어"->TranslateLanguage.SPANISH

            "독일어"->TranslateLanguage.GERMAN

            "불어"->TranslateLanguage.FRENCH

            else->TranslateLanguage.KOREAN

        }


    }

    private fun identifyLang(text:String): String {
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {

                } else {
                    //Log.i(TAG, "Language: $languageCode")

                }
                return@addOnSuccessListener
            }
            .addOnFailureListener {
                // Model couldn’t be loaded or other internal error.
                // ...
            }
        return "und"
    }

}