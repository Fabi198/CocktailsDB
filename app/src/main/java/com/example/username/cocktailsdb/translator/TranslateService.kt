package com.example.username.cocktailsdb.translator

import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions


object TranslateService {

        // on below line we are creating our firebase translate option.
        private val options = TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.SPANISH).build()
        // below line is to get instance
        // for firebase natural language
        val englishSpanishTranslator = Translation.getClient(options)

    init {
        val conditions = DownloadConditions.Builder().requireWifi().build()

        // below line is use to download our modal.
        englishSpanishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener(OnSuccessListener<Void?> { // this method is called when modal is downloaded successfully.
                Log.i("Translator", "Se hizo la descarga")
            }).addOnFailureListener(OnFailureListener {
                Log.i("Translator", "No se hizo la descarga")
            })
    }

}