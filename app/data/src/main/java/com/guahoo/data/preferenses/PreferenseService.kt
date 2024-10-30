package com.guahoo.data.preferenses

import android.content.SharedPreferences

class PreferencesService(private val sharedPreferences: SharedPreferences) {
    var trackIsDownloaded: String?
        get() = sharedPreferences.getString(APP_PREFERENCES_TREKS_IS_DOWNLOADED, null)
        set(value) {
            sharedPreferences.edit().putString(APP_PREFERENCES_TREKS_IS_DOWNLOADED, value).apply()
        }

    companion object {
        const val APP_PREFERENCES = "APP_PREFERENCES"
        const val APP_PREFERENCES_TREKS_IS_DOWNLOADED = "APP_PREFERENCES_TREKS_IS_DOWNLOADED"
    }
}