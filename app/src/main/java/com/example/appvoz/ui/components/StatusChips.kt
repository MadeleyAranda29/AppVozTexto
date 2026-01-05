package com.example.appvoz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatusChips(
    isListening: Boolean,
    detectedLanguage: String,
    isModelDownloading: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.padding(top = 8.dp)) {
            val statusLabel = if (isListening) "Escuchando" else "Inactivo"
            AssistChip(
                onClick = {},
                label = { Text(statusLabel) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )

            if (detectedLanguage.isNotBlank()) {
                AssistChip(
                    onClick = {},
                    label = { Text("Idioma: $detectedLanguage") },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (isModelDownloading) {
                AssistChip(
                    onClick = {},
                    label = { Text("Descargando modelo…") },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (isModelDownloading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
        }
    }
}
