package com.example.weatherbrowser.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.weatherbrowser.BuildConfig
import com.example.weatherbrowser.R
import com.example.weatherbrowser.databinding.FragmentWeatherBinding
import com.example.weatherbrowser.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by viewModels()

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var appPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentWeatherBinding.bind(view)

        val lastSearchedCity = appPreferences.getString("last_searched_city", null)
        if (lastSearchedCity != null) {
            viewModel.fetchWeather(lastSearchedCity, BuildConfig.API_KEY)
            Toast.makeText(
                requireContext(),
                "Loading weather for $lastSearchedCity",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.buttonGetWeather.setOnClickListener {
            val cityName = binding.editTextCity.text.toString()

            if (cityName.isNotEmpty()) {
                appPreferences.edit().putString("last_searched_city", cityName).apply()
                viewModel.fetchWeather(cityName, BuildConfig.API_KEY)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weatherData.collect { weather ->
                    if (weather != null) {
                        Log.d("WeatherFragment", "City weather received: ${weather.main.temp}")

                        // Convert temperature from Kelvin to Celsius
                        val temperatureInCelsius = weather.main.temp - 273.15
                        val feelsLikeCelsius = weather.main.feels_like - 273.15
                        val minTempCelsius = weather.main.temp_min - 273.15
                        val maxTempCelsius = weather.main.temp_max - 273.15

                        binding.textViewCityName.text = weather.name

                        binding.textViewTemperature.text =
                            "Temperature: %.2f °C".format(temperatureInCelsius)
                        binding.textViewFeelsLike.text =
                            "Feels like: %.2f °C".format(feelsLikeCelsius)
                        binding.textViewMinMaxTemp.text =
                            "Min Temp: %.2f °C, Max Temp: %.2f °C".format(
                                minTempCelsius,
                                maxTempCelsius
                            )

                        binding.textViewHumidity.text = "Humidity: ${weather.main.humidity} %"
                        binding.textViewWeatherCondition.text =
                            "Condition: ${weather.weather[0].description}"

                        val iconCode = weather.weather[0].icon
                        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                        Glide.with(this@WeatherFragment)
                            .load(iconUrl)
                            .into(binding.imageViewWeatherIcon)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No weather data available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        //current location weather
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentCityWeatherData.collect { weather ->
                    if (weather != null) {
                        Log.d(
                            "WeatherFragment",
                            "Current city weather received: ${weather.main.temp}"
                        )
                        val temperatureInCelsius = weather.main.temp - 273.15
                        val feelsLikeCelsius = weather.main.feels_like - 273.15
                        val minTempCelsius = weather.main.temp_min - 273.15
                        val maxTempCelsius = weather.main.temp_max - 273.15

                        binding.textViewCurrentCityName.text = weather.name
                        binding.textViewCurrentTemperature.text =
                            "Temperature: %.2f °C".format(temperatureInCelsius)
                        binding.textViewCurrentFeelsLike.text =
                            "Feels like: %.2f °C".format(feelsLikeCelsius)
                        binding.textViewCurrentMinMaxTemp.text =
                            "Min Temp: %.2f °C, Max Temp: %.2f °C".format(
                                minTempCelsius,
                                maxTempCelsius
                            )

                        binding.textViewCurrentHumidity.text =
                            "Humidity: ${weather.main.humidity} %"
                        binding.textViewCurrentCondition.text =
                            "Condition: ${weather.weather[0].description}"

                        val iconCode = weather.weather[0].icon
                        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                        Glide.with(this@WeatherFragment)
                            .load(iconUrl)
                            .into(binding.imageViewCurrentWeatherIcon)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No current city weather data available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        requestLocationPermission()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        Log.d("WeatherFragment", "getCurrentLocation")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    getWeatherForLocation(latitude, longitude)
                } else {
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun getWeatherForLocation(latitude: Double, longitude: Double) {
        Log.d("WeatherFragment", "getWeatherForLocation")
        viewModel.getCurrentLocationWeatherDetails(latitude, longitude, BuildConfig.API_KEY)
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission is required to get weather data.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}