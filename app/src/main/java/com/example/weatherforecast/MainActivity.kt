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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.*
import androidx.navigation.NavController
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

data class WeatherResponse(
    val name: String,
    val coord: Coord,
    val main: Main,
    val weather: List<Weather>
)

data class Coord(
    val lon: Double,
    val lat: Double
)

data class Main(
    val temp: Double,
    val pressure: Double
)

data class Weather(
    val description: String,
    val icon: String
)

private const val API_URL = "https://api.openweathermap.org/data/2.5/"

class WeatherApiParams(
    val city: String = "Warsaw",
    val apiKey: String = "d935a7419da6eb564f3b108aff9771de",
    val units: String = "metric",
    val lang: String = "pl"
)

interface WeatherApi {
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String = WeatherApiParams().city,
        @Query("appid") apiKey: String = WeatherApiParams().apiKey,
        @Query("units") units: String = WeatherApiParams().units,
        @Query("lang") lang: String = WeatherApiParams().lang
    ): WeatherResponse
}

object RetrofitClient {
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}

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
    val items = listOf("extra","weather", "forecast")

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
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            weather = RetrofitClient.api.getWeatherByCity()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Błąd: ${e.localizedMessage}"
            isLoading = false
        }
    }

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            errorMessage != null -> {
                Text("Nie udało się załadować danych.")
                Text(errorMessage ?: "")
            }
            weather != null -> {
                Image(
                    painter = rememberAsyncImagePainter("https://openweathermap.org/img/wn/${weather!!.weather.first().icon}@4x.png"),
                    contentDescription = "Weather icon",
                    modifier = Modifier.size(248.dp),
                    contentScale = ContentScale.Fit
                )
                Text(text = weather!!.name, fontSize = 28.sp)
                Text(text = "${weather!!.main.temp}°C", fontSize = 48.sp)
                Text(text = weather!!.weather.first().description.replaceFirstChar { it.uppercase() }, fontSize = 20.sp)
                Text(text = "lat: " + weather!!.coord.lat + " lon: " + weather!!.coord.lon, fontSize = 18.sp)
            }
        }
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


