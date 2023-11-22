package mysololife.example.sololife.translator

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityTranslateBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Firebase
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        binding= DataBindingUtil.setContentView(this,R.layout.activity_translate)

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

            val englishGermanTranslator = Translation.getClient(options)

            binding.progressBar.visibility = View.VISIBLE
            englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {

                    englishGermanTranslator.translate(binding.input.text.toString())
                        .addOnSuccessListener { translatedText ->

                            binding.output.text=translatedText

                        }
                        .addOnFailureListener { exception ->

                            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()

                        }

                    binding.progressBar.visibility = View.INVISIBLE
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

    private fun detectText() {
        val image = InputImage.fromBitmap(imageBitmap!!, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
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


    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(takePictureIntent.resolveActivity(packageManager)!=null){
            takePictureLauncher.launch(takePictureIntent)
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