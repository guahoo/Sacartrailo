package com.guahoo.data.preferenses

import android.content.SharedPreferences

class PreferencesService(sharedPreferences: SharedPreferences) {
     var cityName by PrefWrapper<String>(
        sharedPreferences,
        APP_PREFERENCES_CITY_NAME_KEY,
        secured = false
    )

    companion object {
        const val APP_PREFERENCES = "APP_PREFERENCES"
        const val APP_PREFERENCES_CITY_NAME_KEY = "APP_PREFERENCES_CITY_NAME_KEY"
    }
}