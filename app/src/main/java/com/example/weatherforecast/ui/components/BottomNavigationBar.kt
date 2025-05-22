package com.example.weatherforecast.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController, items: List<String>) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            val icon = when (screen) {
                "extra" -> "ⓘ"
                "weather" -> "🌡"
                "forecast" -> "☀"
                else -> "error"
            }
            val label = when (screen) {
                "extra" -> "Extra"
                "weather" -> "Weather"
                "forecast" -> "Forecast"
                else -> screen
            }

            NavigationBarItem(
                selected = currentRoute == screen,
                onClick = {
                    if (currentRoute != screen) {
                        navController.navigate(screen) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Text(icon) },
                label = { Text(label) }
            )
        }
    }
}
