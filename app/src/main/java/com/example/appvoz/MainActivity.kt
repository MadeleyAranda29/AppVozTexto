package com.example.appvoz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.appvoz.ui.theme.AppVozTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ML Kit imports
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.common.model.DownloadConditions

class MainActivity : ComponentActivity() {

    // Estado observable para la transcripción y si está escuchando
    private val transcript = mutableStateOf("")
    private val isListening = mutableStateOf(false)

    // Estados para ML Kit
    private val detectedLanguage = mutableStateOf("")
    private val translatedText = mutableStateOf("")

    // Estados para mejorar manejo de permisos y UI
    private val showPermissionRationale = mutableStateOf(false)
    private val permissionDeniedForever = mutableStateOf(false)

    private var speechRecognizer: SpeechRecognizer? = null
    private var translator: Translator? = null

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            runOnUiThread { isListening.value = true }
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            runOnUiThread { isListening.value = false }
        }

        override fun onError(error: Int) {
            runOnUiThread {
                isListening.value = false
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
                transcript.value = msg
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            runOnUiThread {
                if (!matches.isNullOrEmpty()) {
                    transcript.value = matches.joinToString(separator = " ")
                }
                isListening.value = false
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            runOnUiThread {
                if (!partial.isNullOrEmpty()) {
                    transcript.value = partial[0]
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializa SpeechRecognizer si está disponible
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(recognitionListener)
            }
        } else {
            Toast.makeText(this, "Reconocimiento de voz no disponible en este dispositivo", Toast.LENGTH_LONG).show()
        }

        setContent {
            AppVozTheme {
                MainScreen(
                    transcript = transcript.value,
                    isListening = isListening.value,
                    detectedLanguage = detectedLanguage.value,
                    translatedText = translatedText.value,
                    showPermissionRationale = showPermissionRationale.value,
                    permissionDeniedForever = permissionDeniedForever.value,
                    onStart = { startListeningWithPermissionCheck() },
                    onStop = { stopListening() },
                    onDetectLanguage = { identifyLanguage(transcript.value) },
                    onTranslate = { translateToSpanish(transcript.value) },
                    onSavePdf = { saveTranscriptAsPdf(transcript.value) },
                    onConfirmPermissionRequest = { requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onOpenSettings = { openAppSettings() },
                    onDismissPermissionRationale = { showPermissionRationale.value = false }
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { granted ->
            if (granted) {
                permissionDeniedForever.value = false
                startListening()
            } else {
                // Si después de denegar no debemos mostrar rationale, lo consideramos denegado permanente
                permissionDeniedForever.value = !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                Toast.makeText(this, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun startListeningWithPermissionCheck() {
        when {
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startListening()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Mostrar el diálogo Compose de rationale en lugar de un Toast
                showPermissionRationale.value = true
            }
            else -> {
                // Solicitar permiso directamente
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startListening() {
        showPermissionRationale.value = false
        val sr = speechRecognizer
        if (sr == null) {
            Toast.makeText(this, "SpeechRecognizer no está inicializado", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            // Ajusta el idioma si lo deseas, por ejemplo: putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        }

        transcript.value = "Escuchando..."
        sr.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer?.apply {
            stopListening()
            cancel()
        }
        isListening.value = false
    }

    // ML Kit: identificar idioma de un texto
    private fun identifyLanguage(text: String) {
        if (text.isBlank()) {
            detectedLanguage.value = ""
            return
        }
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                runOnUiThread {
                    detectedLanguage.value = if (languageCode == "und") "Desconocido" else languageCode
                }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    detectedLanguage.value = "Error"
                }
            }
    }

    // ML Kit: traducir a español on-device
    private fun translateToSpanish(text: String) {
        if (text.isBlank()) {
            translatedText.value = ""
            return
        }

        // Si ya está en español, no traducir
        if (detectedLanguage.value == "es" || detectedLanguage.value.equals("es-ES", true)) {
            translatedText.value = text
            return
        }

        // Mapear códigos simples a constantes de TranslateLanguage
        val langMap = mapOf(
            "en" to TranslateLanguage.ENGLISH,
            "es" to TranslateLanguage.SPANISH,
            "fr" to TranslateLanguage.FRENCH,
            "pt" to TranslateLanguage.PORTUGUESE,
            "it" to TranslateLanguage.ITALIAN,
            "de" to TranslateLanguage.GERMAN,
            "ru" to TranslateLanguage.RUSSIAN,
            "zh" to TranslateLanguage.CHINESE
        )

        val sourceCode = if (detectedLanguage.value.isNotBlank()) detectedLanguage.value else "und"
        val sourceLang = langMap[sourceCode] ?: run {
            // No podemos traducir si no conocemos la correspondencia
            translatedText.value = "Idioma no soportado para traducción: $sourceCode"
            return
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()

        // Cerrar cualquier traductor previo
        translator?.close()

        translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder().build()
        // Descargar modelo si es necesario
        translator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                // Traducir
                translator?.translate(text)
                    ?.addOnSuccessListener { translated ->
                        runOnUiThread {
                            translatedText.value = translated
                        }
                    }
                    ?.addOnFailureListener { e ->
                        runOnUiThread { translatedText.value = "Error de traducción" }
                    }
            }
            ?.addOnFailureListener { e ->
                runOnUiThread { translatedText.value = "Error al descargar modelo" }
            }
    }

    // Guardar la transcripción actual en PDF y compartir/guardar
    private fun saveTranscriptAsPdf(text: String) {
        val ctx = this
        if (text.isBlank()) {
            Toast.makeText(ctx, "No hay transcripción para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        val docsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (docsDir == null) {
            Toast.makeText(ctx, "No se puede acceder al directorio de documentos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!docsDir.exists()) docsDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "transcripcion_$timestamp.pdf"
        val file = File(docsDir, filename)

        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f * resources.displayMetrics.density
        }

        val x = 20f
        val yStart = 40f
        var y = yStart
        val maxWidth = pageWidth - 40
        val words = text.split(Regex("\\s+"))
        var line = StringBuilder()

        var pageNumber = 1
        var page: PdfDocument.Page = createPage(document, pageWidth, pageHeight, pageNumber)
        var canvas = page.canvas

        fun finishCurrentPageWithFooter(p: PdfDocument.Page) {
            // Dibujar footer en la página actual antes de finalizarla
            val footerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f * resources.displayMetrics.density
            }
            val footerX = 20f
            var footerY = (pageHeight - 50).toFloat()
            canvas.drawText("UNIVERSIDAD NACIONAL DE PIURA", footerX, footerY, footerPaint)
            footerY += footerPaint.textSize + 4f
            canvas.drawText("2025-ALBURQUEQUE ANTON SEGIO", footerX, footerY, footerPaint)
            footerY += footerPaint.textSize + 4f
            canvas.drawText("ARANDA ZAPATA MADELEY", footerX, footerY, footerPaint)
            footerY += footerPaint.textSize + 4f
            canvas.drawText("MORAN PALACIOS NICK", footerX, footerY, footerPaint)

            document.finishPage(p)
        }

        for (w in words) {
            val testLine = if (line.isEmpty()) w else line.toString() + " " + w
            val textWidth = paint.measureText(testLine)
            if (textWidth > maxWidth) {
                canvas.drawText(line.toString(), x, y, paint)
                y += paint.textSize + 6f
                line = StringBuilder(w)
            } else {
                if (line.isEmpty()) line.append(w) else line.append(" ").append(w)
            }

            // si se acerca al final de la página, finalizar y crear nueva página
            if (y > pageHeight - 120) { // dejar espacio para footer
                if (line.isNotEmpty()) {
                    canvas.drawText(line.toString(), x, y, paint)
                    line = StringBuilder()
                }
                finishCurrentPageWithFooter(page)
                pageNumber += 1
                page = createPage(document, pageWidth, pageHeight, pageNumber)
                canvas = page.canvas
                y = yStart
            }
        }

        // dibujar la última línea y finalizar la página
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), x, y, paint)
        }
        finishCurrentPageWithFooter(page)

        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            // Preparar compartir con FileProvider
            val uri: Uri = FileProvider.getUriForFile(ctx, "$packageName.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))

            Toast.makeText(ctx, "PDF guardado: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(ctx, "Error al crear el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Helper para crear una página nueva
    private fun createPage(document: PdfDocument, width: Int, height: Int, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageNumber).create()
        return document.startPage(pageInfo)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        speechRecognizer = null
        translator?.close()
        translator = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    transcript: String,
    isListening: Boolean,
    detectedLanguage: String,
    translatedText: String,
    showPermissionRationale: Boolean,
    permissionDeniedForever: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDetectLanguage: () -> Unit,
    onTranslate: () -> Unit,
    onSavePdf: () -> Unit,
    onConfirmPermissionRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissPermissionRationale: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // Layout principal: Header / Body / Footer
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header profesional con tarjeta e icono
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(ComposeColor(0xFFEFF7FF))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Mic",
                        modifier = Modifier.size(48.dp),
                        tint = ComposeColor(0xFF0066CC)
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Column {
                        Text(text = "Transcripción de voz", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Convierte tu voz a texto y traduce con ML Kit", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Body (contenido central)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = ComposeColor(0xFFF8FAFC), modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = transcript.ifBlank { "Aquí aparecerá la transcripción" }, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                // Indicador de estado
                Text(text = if (isListening) "Estado: Escuchando..." else "Estado: Inactivo", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.padding(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isListening) {
                        Button(onClick = onStart) {
                            Text("Iniciar")
                        }
                    } else {
                        Button(onClick = onStop) {
                            Text("Detener")
                        }
                    }

                    Button(onClick = onDetectLanguage) {
                        Text("Detectar idioma")
                    }

                    Button(onClick = onTranslate) {
                        Text("Traducir")
                    }

                    Button(onClick = onSavePdf) {
                        Text("Descargar PDF")
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                // Mostrar idioma detectado y traducción
                if (detectedLanguage.isNotBlank()) {
                    Text(text = "Idioma detectado: $detectedLanguage", style = MaterialTheme.typography.bodySmall)
                }
                if (translatedText.isNotBlank()) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Traducción:", style = MaterialTheme.typography.bodyMedium)
                    Text(text = translatedText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp))
                }

                Spacer(modifier = Modifier.padding(8.dp))

                // Si el permiso fue denegado permanentemente, ofrecer abrir ajustes
                if (permissionDeniedForever) {
                    Text(text = "Permiso de micrófono denegado permanentemente.")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(onClick = onOpenSettings) {
                        Text("Abrir ajustes")
                    }
                }
            }

            // Footer: las cuatro líneas, cada una en su propia línea (centradas)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = "UNIVERSIDAD NACIONAL DE PIURA", style = MaterialTheme.typography.bodySmall)
                Text(text = "2025-ALBURQUEQUE ANTON SEGIO", style = MaterialTheme.typography.bodySmall)
                Text(text = "ARANDA ZAPATA MADELEY", style = MaterialTheme.typography.bodySmall)
                Text(text = "MORAN PALACIOS NICK", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }

        // Diálogo de rationale para explicar por qué necesitamos el permiso
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { /* no-op: mantener hasta que el usuario responda */ },
                title = { Text(text = "Permiso de micrófono") },
                text = { Text(text = "La aplicación necesita acceso al micrófono para convertir tu voz a texto y usar ML Kit para procesar la transcripción.") },
                confirmButton = {
                    TextButton(onClick = onConfirmPermissionRequest) {
                        Text("Permitir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onDismissPermissionRationale() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppVozTheme {
        MainScreen(
            transcript = "Hola (preview)",
            isListening = false,
            detectedLanguage = "",
            translatedText = "",
            showPermissionRationale = false,
            permissionDeniedForever = false,
            onStart = {},
            onStop = {},
            onDetectLanguage = {},
            onTranslate = {},
            onSavePdf = {},
            onConfirmPermissionRequest = {},
            onOpenSettings = {},
            onDismissPermissionRationale = {}
        )
    }
}