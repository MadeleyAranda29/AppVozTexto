package com.example.appvoz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionButtons(
    isListening: Boolean,
    selectedTargetLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTranslate: () -> Unit,
    onSavePdf: () -> Unit
) {
    BottomAppBar {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        ) {

                // El selector ocupa el espacio disponible para asegurar visibilidad del idioma
                Box(modifier = Modifier.weight(1f)) {
                    LanguageSelector(selectedCode = selectedTargetLanguage, onLanguageSelected = onLanguageSelected)
                }
                Spacer(modifier = Modifier.width(4.dp ))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onTranslate) { androidx.compose.material3.Text("Traducir") }
                    Button(onClick = onSavePdf) { androidx.compose.material3.Text("Descargar PDF") }
                    RecordButton(isListening = isListening, onStart = onStart, onStop = onStop)
                }

            }
    }
}
