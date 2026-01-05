package com.example.appvoz.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.example.appvoz.data.LanguagePrefs
import com.example.appvoz.ml.MLKitManager
import com.example.appvoz.pdf.PdfExporter
import com.example.appvoz.speech.SpeechManager
import com.example.appvoz.ui.theme.AppVozTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

fun startApp(activity: ComponentActivity) {
    activity.enableEdgeToEdge()

    // Estados
    val transcript = mutableStateOf("")
    val isListening = mutableStateOf(false)
    val detectedLanguage = mutableStateOf("")
    val translatedText = mutableStateOf("")
    val selectedTargetLanguage = mutableStateOf("es")
    val showPermissionRationale = mutableStateOf(false)
    val permissionDeniedForever = mutableStateOf(false)
    val isModelDownloading = mutableStateOf(false)

    // Managers
    val mlManager = MLKitManager()
    val speechManager = SpeechManager(
        activity,
        onReady = { activity.runOnUiThread { isListening.value = true } },
        onResults = { result ->
            activity.runOnUiThread {
                transcript.value = result
                isListening.value = false
                identifyLanguage(mlManager, transcript.value) { code -> detectedLanguage.value = code }
            }
        },
        onPartial = { partial ->
            activity.runOnUiThread {
                transcript.value = partial
                identifyLanguage(mlManager, transcript.value) { code -> detectedLanguage.value = code }
            }
        },
        onError = { msg -> activity.runOnUiThread { isListening.value = false; transcript.value = msg } },
        onEnd = { activity.runOnUiThread { isListening.value = false } }
    )

    val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            permissionDeniedForever.value = false
            startListening(showPermissionRationale, speechManager)
        } else {
            permissionDeniedForever.value = !activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
            Toast.makeText(activity, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar idioma persistido
    activity.lifecycleScope.launch {
        LanguagePrefs.selectedLanguageFlow(activity).collectLatest { code ->
            selectedTargetLanguage.value = code
        }
    }

    activity.setContent {
        AppVozTheme {
            MainScreen(
                transcript = transcript.value,
                isListening = isListening.value,
                detectedLanguage = detectedLanguage.value,
                translatedText = translatedText.value,
                showPermissionRationale = showPermissionRationale.value,
                permissionDeniedForever = permissionDeniedForever.value,
                selectedTargetLanguage = selectedTargetLanguage.value,
                onLanguageSelected = { code ->
                    selectedTargetLanguage.value = code
                    activity.lifecycleScope.launch { LanguagePrefs.saveSelectedLanguage(activity, code) }
                },
                onStart = { startListeningWithPermissionCheck(activity, showPermissionRationale, requestPermissionLauncher, speechManager) },
                onStop = { speechManager.stopListening() },
                onTranslateToSelected = {
                    translateToSelected(
                        mlManager,
                        transcript.value,
                        detectedLanguage.value,
                        selectedTargetLanguage.value,
                        onDownloading = { downloading -> isModelDownloading.value = downloading }
                    ) { translated -> translatedText.value = translated }
                },
                onSavePdf = { PdfExporter.saveTranscriptAsPdf(activity, activity.packageName, transcript.value) },
                onConfirmPermissionRequest = { requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onOpenSettings = { openAppSettings(activity) },
                onDismissPermissionRationale = { showPermissionRationale.value = false },
                isModelDownloading = isModelDownloading.value
            )
        }
    }

    // Limpieza al destruir la actividad
    activity.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
            super.onDestroy(owner)
            speechManager.destroy()
            mlManager.close()
        }
    })
}

private fun startListeningWithPermissionCheck(
    activity: ComponentActivity,
    showPermissionRationale: androidx.compose.runtime.MutableState<Boolean>,
    requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    speechManager: SpeechManager
) {
    when {
        activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
            startListening(showPermissionRationale, speechManager)
        }
        activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
            showPermissionRationale.value = true
        }
        else -> {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

private fun startListening(
    showPermissionRationale: androidx.compose.runtime.MutableState<Boolean>,
    speechManager: SpeechManager?
) {
    showPermissionRationale.value = false
    speechManager?.startListening()
}

private fun identifyLanguage(mlManager: MLKitManager, text: String, onDetected: (String) -> Unit) {
    if (text.isBlank()) { onDetected(""); return }
    mlManager.identifyLanguage(text,
        onSuccess = { code -> onDetected(code) },
        onFailure = { onDetected("Error") }
    )
}

private fun translateToSelected(
    mlManager: MLKitManager,
    text: String,
    detectedLanguage: String,
    targetLang: String,
    onDownloading: (Boolean) -> Unit,
    onResult: (String) -> Unit
) {
    if (text.isBlank()) { onResult(""); return }
    mlManager.translateTo(text, detectedLanguage, targetLang,
        onSuccess = { translated -> onResult(translated) },
        onFailure = { err -> onResult(err) },
        onModelDownload = onDownloading
    )
}

private fun openAppSettings(activity: ComponentActivity) {
    val intent = android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", activity.packageName, null)
    }
    activity.startActivity(intent)
}
