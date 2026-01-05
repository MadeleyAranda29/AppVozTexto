package com.example.appvoz.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val PREFS_NAME = "language_prefs"
private const val KEY_SELECTED_LANG = "selected_language_code"

object LanguagePrefs {
    private val inMemoryFlow = MutableStateFlow("es")

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun selectedLanguageFlow(context: Context): Flow<String> {
        val current = prefs(context).getString(KEY_SELECTED_LANG, null) ?: "es"
        inMemoryFlow.value = current
        return inMemoryFlow
    }

    fun saveSelectedLanguage(context: Context, code: String) {
        prefs(context).edit().putString(KEY_SELECTED_LANG, code).apply()
        inMemoryFlow.value = code
    }
}
