package com.example.weatherforecast.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.weatherforecast.RetrofitClient
import com.example.weatherforecast.data.ForecastResponse
import com.example.weatherforecast.data.PreferencesManager
import com.example.weatherforecast.ui.components.ForecastDayCard
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.maxOfOrNull
import kotlin.collections.minOfOrNull


@Composable
fun ForecastScreen(modifier: Modifier = Modifier) {
    var forecast by remember { mutableStateOf<ForecastResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var isOffline by remember { mutableStateOf(false) }

    val currentCityId = prefs.getCurrentCity()
    val cityParts = currentCityId.split("|")
    val lat = cityParts.getOrNull(3)?.toDoubleOrNull()
    val lon = cityParts.getOrNull(4)?.toDoubleOrNull()

    LaunchedEffect(Unit) {
        if (lat == null || lon == null) {
            error = "No valid city coordinates"
            isLoading = false
            return@LaunchedEffect
        }
        try {
            forecast = RetrofitClient.api.getForecastByCoordinates(lat, lon)
            isLoading = false
        } catch (e: Exception) {
            error = e.localizedMessage
            val savedWeather = prefs.getLastSavedWeather()
            if (savedWeather != null || e.message?.contains("Unable to resolve host") == true) {
                isOffline = true
            }
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.padding(40.dp))
        Text(
            text = "5-Day Forecast for ${cityParts.getOrNull(0)?.replaceFirstChar { it.uppercase() } ?: "Unknown"}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isOffline) {
            Text(
                text = "Offline mode - connect to the internet to receive forecast",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text("Error: $error")
                forecast != null -> {
                    val groupedForecast = forecast!!.list.groupBy { it.dt_txt.substring(0, 10) }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            bottom = 92.dp,
                            top = 16.dp
                        )
                    ) {
                        groupedForecast.keys.take(5).forEach { date ->
                            val dayForecasts = groupedForecast[date] ?: emptyList()

                            val dailyMinTemp = dayForecasts.minOfOrNull { it.main.temp_min } ?: 0.0
                            val dailyMaxTemp = dayForecasts.maxOfOrNull { it.main.temp_max } ?: 0.0

                            val representativeForecast = dayForecasts.firstOrNull { it.dt_txt.contains("12:00:00") } ?: dayForecasts.first()

                            item {
                                ForecastDayCard(
                                    date = date,
                                    weather = representativeForecast.weather.first(),
                                    temp_min = dailyMinTemp,
                                    temp_max = dailyMaxTemp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(180.dp))
        }
    }
}
