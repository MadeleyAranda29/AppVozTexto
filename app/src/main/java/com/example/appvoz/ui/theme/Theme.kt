package com.example.appvoz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HighContrastLightColors = lightColorScheme(
    primary = Color(0xFF0B57D0),        // Azul intenso
    onPrimary = Color.White,
    secondary = Color(0xFF006C47),      // Verde oscuro
    onSecondary = Color.White,
    tertiary = Color(0xFF8E24AA),       // Púrpura intenso
    onTertiary = Color.White,
    background = Color(0xFFFFFFFF),     // Blanco puro
    onBackground = Color(0xFF000000),   // Negro
    surface = Color(0xFFFFFFFF),        // Blanco
    onSurface = Color(0xFF000000),      // Negro
    surfaceVariant = Color(0xFFE6E6E6), // Gris claro para contrastes
    onSurfaceVariant = Color(0xFF1A1A1A),
    outline = Color(0xFF5A5A5A)
)

@Composable
fun AppVozTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HighContrastLightColors,
        typography = Typography,
        content = content
    )
}