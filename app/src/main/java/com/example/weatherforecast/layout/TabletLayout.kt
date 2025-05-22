package com.example.weatherforecast.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecast.ui.screens.CitiesScreen
import com.example.weatherforecast.ui.screens.ForecastScreen
import com.example.weatherforecast.ui.screens.SettingsScreen
import com.example.weatherforecast.ui.screens.WeatherScreen
import com.example.weatherforecast.ui.screens.ExtraInformationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletLayout() {
    val navController = rememberNavController()

    var expanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("topbar") },
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("weather") {
                                    popUpTo("weather") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "BackButton",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { expanded = !expanded}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false}
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        navController.navigate("cities") {
                                            popUpTo(navController.graph.startDestinationId){
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    text = { Text("Cities")}
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.startDestinationId){
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    text = { Text("Settings") }
                                )
                            }
                        }
                    }
                }
            )
        }
    )
    { innerPadding ->
        NavHost(
            navController,
            startDestination = "weather",
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            composable("settings") { SettingsScreen(navController) }
            composable("cities") { CitiesScreen(navController) }
            composable("weather") {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExtraInformationScreen(modifier = Modifier.weight(2f))
                    WeatherScreen(modifier = Modifier.weight(3f))
                    ForecastScreen(modifier = Modifier.weight(3f))
                }
            }
        }
    }
}