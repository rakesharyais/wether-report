package com.example.weatherbrowser.Repository

import android.util.Log
import com.example.weatherbrowser.api.WeatherApi
import com.example.weatherbrowser.model.GeoLocationResponse
import com.example.weatherbrowser.model.WeatherResponse
import javax.inject.Inject


class WeatherRepository @Inject constructor(private val api: WeatherApi) {

    suspend fun getCoordinates(cityName: String, apiKey: String): GeoLocationResponse? {
        Log.d("rakesh","WeatherRepository:: $cityName ... $apiKey")
        return api.getCoordinates(cityName, 1, apiKey).firstOrNull()
    }

    suspend fun getWeatherDetails(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): WeatherResponse {
        Log.d("rakesh","WeatherRepository:: getWeatherDetails")
        return api.getWeatherDetails(latitude, longitude, apiKey)
    }

}

