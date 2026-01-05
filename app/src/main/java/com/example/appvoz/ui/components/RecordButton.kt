package com.example.appvoz.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun RecordButton(
    isListening: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    IconButton(onClick = { if (isListening) onStop() else onStart() }) {
        val icon = if (isListening) Icons.Filled.Pause else Icons.Filled.PlayArrow
        val contentDesc = if (isListening) "Pausar" else "Reproducir"
        Icon(imageVector = icon, contentDescription = contentDesc, tint = MaterialTheme.colorScheme.primary)
    }
}
