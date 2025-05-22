package com.example.weatherforecast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.weatherforecast.data.PreferencesManager
import com.example.weatherforecast.data.WeatherApi
import com.example.weatherforecast.data.WeatherApiParams
import com.example.weatherforecast.ui.MainScreen
import com.example.weatherforecast.ui.theme.WeatherForecastTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val API_URL = "https://api.openweathermap.org/data/2.5/"

object RetrofitClient {
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}

class MainActivity : ComponentActivity(), DefaultLifecycleObserver {
    private lateinit var prefs: PreferencesManager
    private var refreshJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ComponentActivity>.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = PreferencesManager(this)
        //prefs.clearSavedCitiesAndWeather()
        WeatherApiParams.units = prefs.getUnits()
        WeatherApiParams.lang = prefs.getLanguage()

        lifecycle.addObserver(this)

//        val currentCity = prefs.getCurrentCity()
//        WeatherApiParams.city = if (currentCity.isBlank()) "warsaw" else currentCity
        setContent {
            WeatherForecastTheme {
                MainScreen()
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        startWeatherRefreshLoop()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopWeatherRefreshLoop()
    }

    override fun onDestroy() {
        super<ComponentActivity>.onDestroy()
        lifecycle.removeObserver(this)
    }

    private fun startWeatherRefreshLoop() {
        if (refreshJob?.isActive == true) return

        refreshJob = CoroutineScope(Dispatchers.IO).launch {
            delay(5 * 60 * 1000L)
            while (isActive) {
                val cityId = prefs.getCurrentCity()
                val parts = cityId.split("|")
                val lat = parts.getOrNull(3)?.toDoubleOrNull()
                val lon = parts.getOrNull(4)?.toDoubleOrNull()

                if (lat != null && lon != null) {
                    try {
                        val weather = RetrofitClient.api.getWeatherByCoordinates(lat, lon)
                        prefs.saveWeatherData(cityId, weather)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "Weather refreshed for $cityId", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "Failed to refresh weather: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                delay(5 * 60 * 1000L)
            }
        }
    }

    private fun stopWeatherRefreshLoop() {
        refreshJob?.cancel()
        refreshJob = null
    }
}

