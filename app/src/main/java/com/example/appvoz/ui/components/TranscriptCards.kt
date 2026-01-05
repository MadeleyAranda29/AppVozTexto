package com.example.appvoz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TranscriptCard(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = text.ifBlank { "Aquí aparecerá la transcripción" }, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun TranslationCard(text: String) {
    if (text.isBlank()) return
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Traducción:", style = MaterialTheme.typography.bodyMedium)
            Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

