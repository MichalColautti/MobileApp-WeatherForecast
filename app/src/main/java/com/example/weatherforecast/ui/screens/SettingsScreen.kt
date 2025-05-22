package com.example.weatherforecast.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.weatherforecast.RetrofitClient
import com.example.weatherforecast.data.PreferencesManager
import com.example.weatherforecast.data.WeatherApiParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var checked by rememberSaveable { mutableStateOf(prefs.getUnits() == "metric") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings", fontSize = 32.sp)

        Text("Units: ")
        Switch(
            checked = checked,
            onCheckedChange = { isChecked ->
                checked = !checked
                if (checked) {
                    WeatherApiParams.units = "metric"
                    prefs.saveUnits("metric")
                } else {
                    WeatherApiParams.units = "imperial"
                    prefs.saveUnits("imperial")
                }
            }
        )
        Text(if (WeatherApiParams.units == "metric") "Celsius" else "Fahrenheit")

        Spacer(modifier = Modifier.height(16.dp))

//        var expanded by rememberSaveable { mutableStateOf(false) }
//        var languages = listOf("English" to "en", "Polski" to "pl")
//        var buttonWidth by remember { mutableIntStateOf(0) }
//
//        Column {
//            Button(
//                onClick = { expanded = !expanded },
//                modifier = Modifier
//                    .onGloballyPositioned { coordinates ->
//                        buttonWidth = coordinates.size.width
//                    }
//            ) {
//                Text("Language: ${languages.find { it.second == WeatherApiParams.lang }?.first ?: "English"}")
//            }
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false },
//                modifier = Modifier
//                    .width(with(LocalDensity.current) { buttonWidth.toDp() })
//                    .offset(y = 8.dp)
//            ) {
//                languages.forEach { (lang, short) ->
//                    DropdownMenuItem(
//                        onClick = {
//                            expanded = false
//                            WeatherApiParams.lang = short
//                            prefs.saveLanguage(short)
//                        },
//                        text = { Text(lang) }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val cityId = prefs.getCurrentCity()
                        val parts = cityId.split("|")
                        val lat = parts.getOrNull(3)?.toDoubleOrNull()
                        val lon = parts.getOrNull(4)?.toDoubleOrNull()

                        if (lat != null && lon != null) {
                            val weather = RetrofitClient.api.getWeatherByCoordinates(lat, lon)
                            prefs.saveWeatherData(cityId, weather)
                        } else {
                            throw IllegalStateException("No city selected or invalid format")
                        }

                        withContext(Dispatchers.Main) {
                            isLoading = false
                            Toast.makeText(context, "Data refreshed successfully", Toast.LENGTH_SHORT).show()
                            navController.navigate("weather") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            Toast.makeText(context, "Refresh failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refreshing...")
            } else {
                Text("Refresh Weather Data")
            }
        }
    }
}
