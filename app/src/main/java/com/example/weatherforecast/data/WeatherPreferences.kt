package com.example.weatherforecast.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.weatherforecast.data.WeatherResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings",Context.MODE_PRIVATE)

    fun saveUnits(units: String) {
        prefs.edit() { putString("units", units) }
    }

    fun getUnits(): String {
        return prefs.getString("units", "metric") ?: "metric"
    }

    fun getLanguage(): String {
        return prefs.getString("language", "pl") ?: "pl"
    }

    fun saveCityList(cityList: List<String>) {
        prefs.edit { putString("cities", cityList.joinToString(",")) }
    }

    fun getCityList(): List<String> {
        val saved = prefs.getString("cities", null) ?: return emptyList()
        return saved.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    fun addCity(id: String) {
        val normalized = id.trim()
        val currentList = getCityList().toMutableList()
        if (!currentList.contains(normalized)) {
            currentList.add(normalized)
            saveCityList(currentList)
        }
    }

    fun removeCity(id: String) {
        val normalized = id.trim()
        val currentList = getCityList().toMutableList()
        if (currentList.contains(normalized)) {
            currentList.remove(normalized)
            saveCityList(currentList)
        }
    }

    fun setCurrentCity(city: String) {
        prefs.edit { putString("current_city", city) }
    }

    fun getCurrentCity(): String {
        return prefs.getString("current_city", "") ?: ""
    }

    fun saveWeatherData(city: String, weatherData: WeatherResponse) {
        val json = Gson().toJson(weatherData)
        prefs.edit { putString("weather_$city", json) }
    }

    fun getWeatherData(city: String): WeatherResponse? {
        val json = prefs.getString("weather_$city", null) ?: return null
        return try {
            Gson().fromJson(json, WeatherResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getLastSavedWeather(): WeatherResponse? {
        val city = getCurrentCity()
        if (city.isBlank()) return null
        return getWeatherData(city)
    }

    fun clearSavedCitiesAndWeather() {
        prefs.edit {
            getCityList().forEach { cityId ->
                remove("weather_$cityId")
            }

            remove("cities")
            remove("current_city")
        }
    }

    fun removeWeatherData(cityId: String) {
        prefs.edit {
            remove("weather_$cityId")
        }
    }
}