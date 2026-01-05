package com.example.appvoz.model

/**
 * Modelo de datos para una transcripción y su traducción asociada.
 * Útil si se desea persistir o intercambiar este estado entre capas.
 */
data class TranscriptData(
    val rawText: String,
    val detectedLanguageCode: String = "",
    val translatedText: String = "",
    val targetLanguageCode: String = ""
) {
    val hasText: Boolean get() = rawText.isNotBlank()
    val hasTranslation: Boolean get() = translatedText.isNotBlank()
}

