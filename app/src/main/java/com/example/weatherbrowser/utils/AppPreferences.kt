package com.example.weatherbrowser.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun saveLastSearchedCity(cityName: String) {
        sharedPreferences.edit().putString("last_searched_city", cityName).apply()
    }

    fun getLastSearchedCity(): String? {
        return sharedPreferences.getString("last_searched_city", null)
    }

    fun clearLastSearchedCity() {
        sharedPreferences.edit().remove("last_searched_city").apply()
    }
}

