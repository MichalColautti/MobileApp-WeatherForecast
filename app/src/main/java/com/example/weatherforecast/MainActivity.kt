package com.example.weatherforecast

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.compose.rememberAsyncImagePainter
import androidx.core.content.edit

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

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings",Context.MODE_PRIVATE)

    fun saveUnits(units: String) {
        prefs.edit() { putString("units", units) }
    }

    fun getUnits(): String {
        return prefs.getString("units", "metric") ?: "metric"
    }

    fun saveLanguage(lang: String) {
        prefs.edit() { putString("language", lang) }
    }

    fun getLanguage(): String {
        return prefs.getString("language", "pl") ?: "pl"
    }

    fun saveCityList(cityList: List<String>) {
        prefs.edit { putString("cities", cityList.joinToString(",")) }
    }

    fun getCityList(): List<String> {
        val saved = prefs.getString("cities", null) ?: "Warsaw"
        return saved.split(",").distinct()
    }

    fun setCurrentCity(city: String) {
        prefs.edit { putString("current_city", city) }
    }

    fun getCurrentCity(): String {
        return prefs.getString("current_city", "Warsaw") ?: "Warsaw"
    }
}

private const val API_URL = "https://api.openweathermap.org/data/2.5/"

object WeatherApiParams{
    var city: String = "Warsaw"
    val apiKey: String = "d935a7419da6eb564f3b108aff9771de"
    var units: String = "metric"
    var lang: String = "pl"
}

interface WeatherApi {
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String = WeatherApiParams.city,
        @Query("appid") apiKey: String = WeatherApiParams.apiKey,
        @Query("units") units: String = WeatherApiParams.units,
        @Query("lang") lang: String = WeatherApiParams.lang
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
        val prefs = PreferencesManager(this)
        WeatherApiParams.units = prefs.getUnits()
        WeatherApiParams.lang = prefs.getLanguage()
        WeatherApiParams.city = prefs.getCurrentCity()
        setContent {
            WeatherForecastTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val items = listOf("extra","weather", "forecast")

    var expanded by rememberSaveable { mutableStateOf(false)}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = { expanded = !expanded}) {
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
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, items)
        }
    )
    { innerPadding ->
        NavHost(
            navController,
            startDestination = "weather",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("extra") { ExtraInformationScreen() }
            composable("weather") { WeatherForecastScreen() }
            composable("forecast") { ForecastScreen() }
            composable("settings") { SettingsScreen() }
            composable("cities") { CitiesScreen(navController) }
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

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var checked by rememberSaveable { mutableStateOf(prefs.getUnits() == "metric") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Settings", fontSize = 32.sp)
        Text("Units: ")
        Switch(
            checked = checked,
            onCheckedChange = { isChecked ->
                checked = !checked
                if (checked){
                    WeatherApiParams.units = "metric"
                    prefs.saveUnits("metric")
                }
                else {
                    WeatherApiParams.units = "imperial"
                    prefs.saveUnits("imperial")
                }
            }
        )
        Text(if (WeatherApiParams.units == "metric") "Celsius" else "Fahrenheit")

        Spacer(modifier = Modifier.height(16.dp))

        var expanded by rememberSaveable { mutableStateOf(false) }
        var languages = listOf("English" to "en", "Polski" to "pl")
        var buttonWidth by remember { mutableIntStateOf(0) }

        Column {
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        buttonWidth = coordinates.size.width
                    }
            ) {
                Text("Language: ${languages.find { it.second == WeatherApiParams.lang }?.first ?: "English"}")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { buttonWidth.toDp() })
                    .offset(y = 8.dp)
            ) {
                languages.forEach { (lang,short) ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            WeatherApiParams.lang = short
                            prefs.saveLanguage(short)
                        },
                        text = { Text(lang) }
                    )
                }
            }
        }

    }
}

@Composable
fun CitiesScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var newCity by rememberSaveable { mutableStateOf("") }
    var cityList by rememberSaveable { mutableStateOf(prefs.getCityList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Cities", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newCity,
            onValueChange = { newCity = it },
            label = { Text("Add city") }
        )

        Button(onClick = {
            if (newCity == "") {
                Toast.makeText(context,"Empty city field", Toast.LENGTH_SHORT).show()
            }
            else {
                try {
                    val city = newCity.trim()
                    prefs.setCurrentCity(city)

                    WeatherApiParams.city = city

                    navController.navigate("weather") {
                        popUpTo("weather") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
        {
            Text("Go to")
        }
    }
}


@Composable
fun ExtraInformationScreen() {
    Column(
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

    Column(
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
                Text(text = "${weather!!.main.temp} " + if (WeatherApiParams.units == "metric") "°C" else "°F", fontSize = 48.sp)
                Text(text = weather!!.weather.first().description.replaceFirstChar { it.uppercase() }, fontSize = 20.sp)
                Text(text = "lat: " + weather!!.coord.lat + " lon: " + weather!!.coord.lon, fontSize = 18.sp)
            }
        }
    }
}


@Composable
fun ForecastScreen() {
    Column(
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


