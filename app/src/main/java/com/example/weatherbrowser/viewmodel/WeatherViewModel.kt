package com.example.weatherbrowser.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbrowser.Repository.WeatherRepository
import com.example.weatherbrowser.model.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    private val _currentCityWeatherData = MutableStateFlow<WeatherResponse?>(null)
    val currentCityWeatherData: StateFlow<WeatherResponse?> = _currentCityWeatherData

    fun fetchWeather(cityName: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val geoData = repository.getCoordinates(cityName, apiKey)
                geoData?.let {
                    val weather = repository.getWeatherDetails(it.lat, it.lon, apiKey)
                    _weatherData.value = weather
                }
            } catch (e: Exception) {
                Log.d("WeatherViewModel","Exception = ${e.message}")
            }
        }
    }


    fun getCurrentLocationWeatherDetails(latitude: Double, longitude: Double,apikey: String) {
        viewModelScope.launch {
            val weather = repository.getWeatherDetails(latitude, longitude, apikey)
            _currentCityWeatherData.value = weather
        }
    }


}
