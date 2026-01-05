package com.example.appvoz.ml

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.DownloadConditions

/**
 * MLKitManager encapsula las llamadas a ML Kit para detección de idioma y traducción.
 * No realiza operaciones sobre la UI; en su lugar invoca callbacks para que el llamador
 * actualice el estado en el hilo adecuado.
 */
class MLKitManager {
    private var translator: Translator? = null

    private fun normalizeCode(code: String): String = code.lowercase().substringBefore('-')

    fun identifyLanguage(text: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        if (text.isBlank()) { onSuccess(""); return }
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                // Si es "und" (indeterminado), devolvemos vacío para no romper el mapeo
                onSuccess(if (languageCode == "und") "" else normalizeCode(languageCode))
            }
            .addOnFailureListener { e -> onFailure(Exception(e)) }
    }

    fun translateToSpanish(
        text: String,
        detectedLanguage: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        onModelDownload: ((Boolean) -> Unit)? = null
    ) {
        if (text.isBlank()) { onSuccess(""); return }
        val srcTag = TranslateLanguage.fromLanguageTag(normalizeCode(detectedLanguage))
            ?: return onFailure("Idioma fuente no soportado")
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(srcTag)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
        translator?.close()
        translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()
        onModelDownload?.invoke(true)
        translator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                onModelDownload?.invoke(false)
                translator?.translate(text)
                    ?.addOnSuccessListener { translated -> onSuccess(translated) }
                    ?.addOnFailureListener { onFailure("Error de traducción") }
            }
            ?.addOnFailureListener {
                onModelDownload?.invoke(false)
                onFailure("Error al descargar modelo")
            }
    }

    /**
     * Traducción genérica al idioma destino indicado por código ISO (ej: "en", "es").
     */
    fun translateTo(
        text: String,
        detectedLanguage: String,
        targetLanguageCode: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        onModelDownload: ((Boolean) -> Unit)? = null
    ) {
        if (text.isBlank()) { onSuccess(""); return }
        val src = TranslateLanguage.fromLanguageTag(normalizeCode(detectedLanguage))
            ?: return onFailure("Idioma fuente no soportado")
        val tgt = TranslateLanguage.fromLanguageTag(normalizeCode(targetLanguageCode))
            ?: return onFailure("Idioma destino no soportado")
        if (src == tgt) { onSuccess(text); return }
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(src)
            .setTargetLanguage(tgt)
            .build()
        translator?.close()
        translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()
        onModelDownload?.invoke(true)
        translator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                onModelDownload?.invoke(false)
                translator?.translate(text)
                    ?.addOnSuccessListener { translated -> onSuccess(translated) }
                    ?.addOnFailureListener { onFailure("Error de traducción") }
            }
            ?.addOnFailureListener {
                onModelDownload?.invoke(false)
                onFailure("Error al descargar modelo")
            }
    }

    fun close() {
        translator?.close()
        translator = null
    }
}
