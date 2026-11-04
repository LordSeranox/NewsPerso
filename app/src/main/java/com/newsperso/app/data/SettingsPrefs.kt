package com.newsperso.app.data

import android.content.Context

class SettingsPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    var language: String
        get() = prefs.getString(KEY_LANG, "fr") ?: "fr"
        set(v) = prefs.edit().putString(KEY_LANG, v).apply()

    var theme: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(v) = prefs.edit().putString(KEY_THEME, v).apply()

    companion object {
        private const val KEY_LANG = "language"
        private const val KEY_THEME = "theme"
    }
}
