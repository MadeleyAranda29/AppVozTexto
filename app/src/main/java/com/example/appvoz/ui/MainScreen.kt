package com.example.appvoz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appvoz.ui.components.ActionButtons
import com.example.appvoz.ui.components.AppFooter
import com.example.appvoz.ui.components.PermissionDialogs
import com.example.appvoz.ui.components.StatusChips
import com.example.appvoz.ui.components.TranscriptCard
import com.example.appvoz.ui.components.TranslationCard
import com.example.appvoz.ui.theme.AppVozTheme

@Composable
fun MainScreen(
    transcript: String,
    isListening: Boolean,
    detectedLanguage: String,
    translatedText: String,
    showPermissionRationale: Boolean,
    permissionDeniedForever: Boolean,
    selectedTargetLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTranslateToSelected: () -> Unit,
    onSavePdf: () -> Unit,
    onConfirmPermissionRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissPermissionRationale: () -> Unit,
    isModelDownloading: Boolean
) {
    Scaffold(modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ActionButtons(
                    isListening = isListening,
                    selectedTargetLanguage = selectedTargetLanguage,
                    onLanguageSelected = onLanguageSelected,
                    onStart = onStart,
                    onStop = onStop,
                    onTranslate = onTranslateToSelected,
                    onSavePdf = onSavePdf
                )
                AppFooter()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    // Icon and text for voice transcription feature
                }
            }

            // Chips de estado: escuchando e idioma
            StatusChips(isListening = isListening, detectedLanguage = detectedLanguage, isModelDownloading = isModelDownloading)

            // Área central desplazable
            val scrollState = rememberScrollState()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                TranscriptCard(text = transcript)

                Spacer(modifier = Modifier.padding(8.dp))

                Text(text = if (isListening) "Estado: Escuchando..." else "Estado: Inactivo", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.padding(12.dp))

                TranslationCard(text = translatedText)

                Spacer(modifier = Modifier.padding(8.dp))

                if (permissionDeniedForever) {
                    Text(text = "Permiso de micrófono denegado permanentemente.")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(onClick = onOpenSettings) {
                        Text("Abrir ajustes")
                    }
                }

                // Añadir espacio extra al final para evitar que el último elemento quede oculto tras la BottomAppBar
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        PermissionDialogs(
            showPermissionRationale = showPermissionRationale,
            permissionDeniedForever = permissionDeniedForever,
            onConfirmPermissionRequest = onConfirmPermissionRequest,
            onOpenSettings = onOpenSettings,
            onDismissPermissionRationale = onDismissPermissionRationale
        )
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
            selectedTargetLanguage = "es",
            onLanguageSelected = {},
            onStart = {},
            onStop = {},
            onTranslateToSelected = {},
            onSavePdf = {},
            onConfirmPermissionRequest = {},
            onOpenSettings = {},
            onDismissPermissionRationale = {},
            isModelDownloading = false
        )
    }
}
