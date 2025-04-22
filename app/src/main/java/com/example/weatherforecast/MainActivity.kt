package com.example.weatherforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.ui.theme.WeatherForecastTheme
import androidx.compose.material3.*
import androidx.navigation.compose.*
import androidx.navigation.NavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherForecastTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf("weather", "extra", "forecast")

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, items)
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = "weather",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("extra") { ExtraInformationScreen() }
            composable("weather") { WeatherForecastScreen() }
            composable("forecast") { ForecastScreen() }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController, items: List<String>) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            val icon = when (screen) {
                "extra" -> "ⓘ"
                "weather" -> "☀"
                "forecast" -> "🌡"
                else -> "❓"
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
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
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

@Composable
fun ExtraInformationScreen() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Extra info", fontSize = 32.sp)
        Text("dane dodatkowe np.: informacje o sile i kierunku wiatru, wilgotności widoczności", fontSize = 20.sp)
    }
}

@Composable
fun WeatherForecastScreen(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(text = "🌤️", fontSize = 64.sp)
        Text(text = "Warszawa", fontSize = 28.sp)
        Text(text = "18°C", fontSize = 48.sp)
        Text(text = "Częściowe zachmurzenie", fontSize = 20.sp)
    }
}

@Composable
fun ForecastScreen() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Forecast", fontSize = 32.sp)
        Text("prognoza pogody na nadchodzące dni", fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WeatherForecastTheme {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherForecastScreen() {
    WeatherForecastTheme {
        WeatherForecastScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExtraInformationScreen() {
    WeatherForecastTheme {
        ExtraInformationScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewForecastScreen() {
    WeatherForecastTheme {
        ForecastScreen()
    }
}


