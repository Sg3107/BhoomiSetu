package com.satya.bhoomisetu.util.localization

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val localizedName: String,
    val badge: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        localizedName = "English",
        badge = "EN"
    ),
    TELUGU(
        code = "te",
        displayName = "Telugu",
        localizedName = "తెలుగు",
        badge = "తె"
    )
}
