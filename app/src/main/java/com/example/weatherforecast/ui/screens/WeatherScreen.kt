package com.example.weatherforecast.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.weatherforecast.RetrofitClient
import com.example.weatherforecast.data.PreferencesManager
import com.example.weatherforecast.data.WeatherResponse
import com.example.weatherforecast.data.WeatherApiParams

@Composable
fun WeatherScreen(modifier: Modifier = Modifier) {
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
            weather = RetrofitClient.api.getWeatherByCoordinates(lat!!, lon!!)

            weather?.let { prefs.saveWeatherData(currentCityId, it) }

            isFavorite = prefs.getCityList().contains(currentCityId)

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error: ${e.localizedMessage}"
            val savedWeather = prefs.getWeatherData(currentCityId)
            if (savedWeather != null) {
                weather = savedWeather as WeatherResponse?
                isOffline = true
            }
            isLoading = false
        }
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
            errorMessage != null && weather == null -> {
                Text("Can't load data.")
                Text(errorMessage ?: "")
            }
            weather != null -> {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        if (isOffline) {
                            Text("Offline mode - data may not be current", color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Image(
                            painter = rememberAsyncImagePainter("https://openweathermap.org/img/wn/${weather!!.weather.first().icon}@4x.png"),
                            contentDescription = "Weather icon",
                            modifier = Modifier.size(248.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(text = weather!!.name, fontSize = 28.sp)
                        Text(text = "${weather!!.main.temp} " + if (WeatherApiParams.units == "metric") "°C" else "°F", fontSize = 48.sp)
                        Text(text = weather!!.weather.first().description.replaceFirstChar { it.uppercase() }, fontSize = 20.sp)
                        Text(text = "lat: " + weather!!.coord.lat + " lon: " + weather!!.coord.lon, fontSize = 18.sp)
                        IconButton(
                            onClick = {
                                isFavorite = !isFavorite
                                if (isFavorite) {
                                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                                    prefs.addCity(currentCityId)
                                    weather?.let { prefs.saveWeatherData(currentCityId, it) }
                                } else {
                                    Toast.makeText(context, "Deleted from favorites", Toast.LENGTH_SHORT).show()
                                    prefs.removeCity(currentCityId)
                                    prefs.removeWeatherData(currentCityId)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) Color.Yellow else Color.Gray,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

            }
        }
    }
}
