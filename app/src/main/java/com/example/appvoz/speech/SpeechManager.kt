package com.example.appvoz.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast

/**
 * SpeechManager encapsula la lógica de SpeechRecognizer y expone operaciones simples
 * startListening/stopListening/destroy. No altera la lógica original: invoca callbacks
 * para notificar eventos (ready, partial, result, error, end).
 */
class SpeechManager(
    private val context: Context,
    private val onReady: () -> Unit,
    private val onResults: (String) -> Unit,
    private val onPartial: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onEnd: () -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        onReady()
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                        onEnd()
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
                            SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
                            SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS"
                            SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                            else -> "Recognition error: $error"
                        }
                        onError(msg)
                        onEnd()
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            // Usar solo la mejor hipótesis para evitar repeticiones
                            onResults(matches[0])
                        }
                        onEnd()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!partial.isNullOrEmpty()) {
                            onPartial(partial[0])
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } else {
            Toast.makeText(context, "Reconocimiento de voz no disponible en este dispositivo", Toast.LENGTH_LONG).show()
        }
    }

    fun startListening() {
        val sr = speechRecognizer
        if (sr == null) {
            Toast.makeText(context, "SpeechRecognizer no está inicializado", Toast.LENGTH_SHORT).show()
            return
        }
        if (isListening) {
            // Ya escuchando; evitar doble inicio que causa ERROR_RECOGNIZER_BUSY
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            // Opcional: usar idioma por defecto del dispositivo
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
        }

        onResults("Escuchando...")
        isListening = true
        sr.startListening(intent)
    }

    fun stopListening() {
        val sr = speechRecognizer ?: return
        if (!isListening) {
            // Evitar stop/cancel cuando no está escuchando, reduce ERROR_CLIENT
            return
        }
        try {
            sr.stopListening()
        } catch (_: Exception) {
            // Ignorar cualquier excepción del cliente
        } finally {
            // No llamar cancel inmediatamente después de stop; esperar a onEndOfSpeech/onResults
            isListening = false
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } finally {
            speechRecognizer = null
            isListening = false
        }
    }
}
