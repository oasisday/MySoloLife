package mysololife.example.sololife.translator

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityTranslateBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions


class TranslateActivity : AppCompatActivity() {
    lateinit var binding: ActivityTranslateBinding
    private var items= arrayOf("ENGLISH","KOREAN","JAPANESE","CHINESE","SPANISH","GERMAN", "FRENCH")
    private var conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

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

            englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {

                    englishGermanTranslator.translate(binding.input.text.toString())
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