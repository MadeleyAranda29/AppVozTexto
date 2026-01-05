package com.example.appvoz.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PermissionDialogs(
    showPermissionRationale: Boolean,
    permissionDeniedForever: Boolean,
    onConfirmPermissionRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissPermissionRationale: () -> Unit
) {
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { /* no-op */ },
            title = { Text(text = "Permiso de micrófono") },
            text = { Text(text = "La app necesita acceso al micrófono para transcribir y traducir tu voz.") },
            confirmButton = {
                TextButton(onClick = onConfirmPermissionRequest) { Text("Permitir") }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionRationale) { Text("Cancelar") }
            }
        )
    }

    if (permissionDeniedForever && !showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { /* no-op */ },
            title = { Text(text = "Permiso denegado permanentemente") },
            text = { Text(text = "Debes habilitar el permiso de micrófono desde Ajustes para continuar.") },
            confirmButton = {
                TextButton(onClick = onOpenSettings) { Text("Abrir ajustes") }
            },
            dismissButton = {
                TextButton(onClick = { /* no-op */ }) { Text("Cerrar") }
            }
        )
    }
}

