package com.example.appvoz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.example.appvoz.model.Languages
import androidx.compose.material3.MenuAnchorType

/**
 * Selector de idioma destino para traducción.
 * Muestra nombres legibles y retorna el código ISO (en, es, fr, ...).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    selectedCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val languages = Languages.supported
    val expanded = remember { mutableStateOf(false) }
    val current = languages.find { it.code == selectedCode } ?: languages.first()

    Column(modifier = modifier.fillMaxWidth().padding(4.dp)) {
        if (showLabel) {
            Text(text = "Idioma de destino")
        }
        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = !expanded.value }
        ) {
            TextField(
                readOnly = true,
                value = "${current.name} (${current.code})",
                onValueChange = {},
                singleLine = true,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(text = "${lang.name} (${lang.code})") },
                        onClick = {
                            onLanguageSelected(lang.code)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}
