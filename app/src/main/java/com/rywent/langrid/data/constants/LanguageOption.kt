package com.rywent.langrid.data.constants

data class LanguageOption(val code: String, val name: String)

val supportedLanguages = listOf(
    LanguageOption("en", "English"),
    LanguageOption("es", "Spanish"),
    LanguageOption("de", "German"),
    LanguageOption("fr", "French"),
    LanguageOption("it", "Italian"),
    LanguageOption("ru", "Russian"),
    LanguageOption("pt", "Portuguese"),
    LanguageOption("ja", "Japanese"),
    LanguageOption("zh", "Chinese"),
    LanguageOption("ko", "Korean"),
    LanguageOption("pl", "Polish"),
    LanguageOption("uk", "Ukrainian"),
    LanguageOption("nl", "Dutch"),
    LanguageOption("tr", "Turkish"),
    LanguageOption("sv", "Swedish"),
    LanguageOption("da", "Danish"),
    LanguageOption("no", "Norwegian"),
    LanguageOption("fi", "Finnish"),
    LanguageOption("cs", "Czech"),
    LanguageOption("ro", "Romanian")
)