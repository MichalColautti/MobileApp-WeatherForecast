package com.example.weatherforecast.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.weatherforecast.data.SearchResult
import com.example.weatherforecast.ui.components.CityCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.split


@Composable
fun CitiesScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var newCity by rememberSaveable { mutableStateOf("") }
    var cityList by remember { mutableStateOf(prefs.getCityList()) }

    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }

    LaunchedEffect(Unit) {
        cityList = prefs.getCityList()
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Wybierz miasto") },
            text = {
                LazyColumn {
                    items(searchResults.size) { index ->
                        val city = searchResults[index]
                        val cityLabel = buildString {
                            append(city.name)
                            city.state?.let { append(", $it") }
                            append(", ${city.country}")
                            append(" [${city.lat}, ${city.lon}]")
                        }

                        Text(
                            text = cityLabel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val selectedCityId = "${city.name}|${city.state ?: ""}|${city.country}|${city.lat}|${city.lon}"
                                    prefs.setCurrentCity(selectedCityId)
                                    showDialog = false
                                    navController.navigate("weather") {
                                        popUpTo("weather") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                                .padding(8.dp)
                        )
                    }

                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.size(56.dp))
            Text("Cities", fontSize = 32.sp)
        }

        item {
            OutlinedTextField(
                value = newCity,
                onValueChange = { newCity = it },
                label = { Text("Add city") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(onClick = {
                if (newCity.isBlank()) {
                    Toast.makeText(context, "Empty city field", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val results = RetrofitClient.api.searchCity(newCity.trim())
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            if (results.isEmpty()) {
                                Toast.makeText(context, "No matching cities found", Toast.LENGTH_SHORT).show()
                            } else if (results.size == 1) {
                                val city = results.first()
                                val selectedCityId = "${city.name}|${city.state ?: ""}|${city.country}|${city.lat}|${city.lon}"
                                prefs.setCurrentCity(selectedCityId)
                                navController.navigate("weather") {
                                    popUpTo("weather") { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                searchResults = results
                                showDialog = true
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            Toast.makeText(context, "Search error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching...")
                } else {
                    Text("Go to")
                }
            }
        }

        item {
            Text("Favorite Cities", fontSize = 24.sp)
        }

        if (cityList.isEmpty()) {
            item {
                Text("No favorite cities yet")
            }
        } else {
            items(cityList.size) { index ->
                val cityParts = cityList[index].split("|")
                val name = cityParts.getOrNull(0) ?: ""
                val state = cityParts.getOrNull(1) ?: ""
                val country = cityParts.getOrNull(2) ?: ""
                val lat = cityParts.getOrNull(3) ?: ""
                val lon = cityParts.getOrNull(4) ?: ""

                val displayName = buildString {
                    append(name)
                    if (state.isNotBlank()) append(", $state")
                    append(", $country")
                    append(" [$lat, $lon]")
                }

                CityCard(
                    city = displayName,
                    onClick = {
                        val selectedCityId = "$name|$state|$country|$lat|$lon"
                        prefs.setCurrentCity(selectedCityId)
                        prefs.addCity(selectedCityId)
                        navController.navigate("weather") {
                            popUpTo("weather") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

        }

        item {
            Spacer(modifier = Modifier.size(80.dp))
        }
    }
}