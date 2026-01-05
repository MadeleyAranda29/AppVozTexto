package com.example.appvoz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppHeader() {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Transcripción de voz", style = MaterialTheme.typography.titleLarge)
        Text(text = "Convierte tu voz a texto y traduce con ML Kit", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AppFooter() {
    Column(modifier = Modifier.padding(8.dp)) {
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "UNIVERSIDAD NACIONAL DE PIURA", style = MaterialTheme.typography.bodySmall)
        Text(text = "2025-ALBURQUEQUE ANTON SEGIO", style = MaterialTheme.typography.bodySmall)
        Text(text = "ARANDA ZAPATA MADELEY", style = MaterialTheme.typography.bodySmall)
        Text(text = "MORAN PALACIOS NICK", style = MaterialTheme.typography.bodySmall)
    }
}

