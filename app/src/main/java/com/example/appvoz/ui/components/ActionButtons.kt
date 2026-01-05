package com.example.appvoz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            LanguageSelector(
                selectedCode = selectedTargetLanguage,
                onLanguageSelected = onLanguageSelected,
                modifier = Modifier
                    .fillMaxWidth(),
                showLabel = false
            )

            Divider()

            // Fila de botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Traducir (con peso para llenar espacio)
                Button(
                    onClick = onTranslate,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Traducir")
                }

                // Botón PDF (con peso para llenar espacio)
                Button(
                    onClick = onSavePdf,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("PDF")
                }

                // El botón de grabar se queda con su tamaño fijo
                RecordButton(
                    isListening = isListening,
                    onStart = onStart,
                    onStop = onStop
                )
            }
        }
    }
}
