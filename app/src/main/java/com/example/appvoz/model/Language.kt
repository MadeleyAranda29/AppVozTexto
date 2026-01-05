package com.example.appvoz.model

data class Language(val code: String, val name: String)

object Languages {
    val supported: List<Language> = listOf(
        Language("es", "Español"),
        Language("en", "Inglés"),
        Language("fr", "Francés"),
        Language("pt", "Portugués"),
        Language("it", "Italiano"),
        Language("de", "Alemán"),
        Language("ru", "Ruso"),
        Language("zh", "Chino"),
        Language("ja", "Japonés"),
        Language("ko", "Coreano")
    )

    fun byCode(code: String): Language? = supported.find { it.code == code }
}

