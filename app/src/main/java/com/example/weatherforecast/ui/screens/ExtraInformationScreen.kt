package com.example.weatherforecast.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.RetrofitClient
import com.example.weatherforecast.data.PreferencesManager
import com.example.weatherforecast.data.WeatherResponse


@Composable
fun ExtraInformationScreen(modifier: Modifier = Modifier) {
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    val currentCityId = prefs.getCurrentCity()
    val cityParts = currentCityId.split("|")
    val name = cityParts.getOrNull(0) ?: ""
    val state = cityParts.getOrNull(1) ?: ""
    val country = cityParts.getOrNull(2) ?: ""
    val lat = cityParts.getOrNull(3)?.toDoubleOrNull()
    val lon = cityParts.getOrNull(4)?.toDoubleOrNull()

    var isFavorite by rememberSaveable { mutableStateOf(false)}
    var isOffline by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (currentCityId.isBlank()) {
            errorMessage = "No city selected"
            isLoading = false
            return@LaunchedEffect
        }
        try {
            isFavorite = prefs.getCityList().contains(currentCityId)

            weather = RetrofitClient.api.getWeatherByCoordinates(lat!!, lon!!)

            weather?.let { prefs.saveWeatherData(currentCityId, it) }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error: ${e.localizedMessage}"
            val savedWeather = prefs.getWeatherData(currentCityId)
            if (savedWeather != null) {
                weather = savedWeather
                isOffline = true
            }
            isLoading = false
        }
    }

    if (weather == null) {
        Text("Can't load data.")
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            weather == null -> Text("No weather data available")
            else -> {
                if (isOffline) {
                    Text("Offline mode - data may not be current", color = Color.Red, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("Wind: ${weather!!.wind.speed} m/s", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Direction: ${weather!!.wind.deg}°", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text("Humidity: ${weather!!.main.pressure} hPa", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Visibility: ${weather!!.visibility / 1000.0} km", fontSize = 20.sp)
            }
        }
    }
}