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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import coil.compose.rememberAsyncImagePainter
import androidx.core.content.edit
import com.google.gson.Gson

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
        val saved = prefs.getString("cities", null) ?: return emptyList()
        return saved.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    fun addCity(city: String) {
            val normalizedCity = city.trim().lowercase()
        val currentList = getCityList().toMutableList()
        if (!currentList.contains(normalizedCity)) {
            currentList.add(normalizedCity)
            saveCityList(currentList)
        }
    }

    fun removeCity(city: String) {
        val normalizedCity = city.trim().lowercase()
        val currentList = getCityList().toMutableList()
        if (currentList.contains(normalizedCity)) {
            currentList.remove(normalizedCity)
            saveCityList(currentList)
        }
    }

    fun setCurrentCity(city: String) {
        prefs.edit { putString("current_city", city) }
    }

    fun getCurrentCity(): String {
        return prefs.getString("current_city", "") ?: ""
    }

    fun saveWeatherData(city: String, weatherData: WeatherResponse) {
        val json = Gson().toJson(weatherData)
        prefs.edit { putString("weather_$city", json) }
    }

    fun getWeatherData(city: String): WeatherResponse? {
        val json = prefs.getString("weather_$city", null) ?: return null
        return try {
            Gson().fromJson(json, WeatherResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getLastSavedWeather(): WeatherResponse? {
        val city = getCurrentCity()
        if (city.isBlank()) return null
        return getWeatherData(city)
    }
}

private const val API_URL = "https://api.openweathermap.org/data/2.5/"

object WeatherApiParams{
    var city: String = "warsaw"
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
        val currentCity = prefs.getCurrentCity()
        WeatherApiParams.city = if (currentCity.isBlank()) "warsaw" else currentCity
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
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            composable("extra") { ExtraInformationScreen() }
            composable("weather") { WeatherScreen() }
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
    var cityList by remember { mutableStateOf(prefs.getCityList()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
        item {
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
                if (newCity == "") {
                    Toast.makeText(context,"Empty city field", Toast.LENGTH_SHORT).show()
                }
                else {
                    try {
                        val city = newCity.trim().lowercase()
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

        item {
            Text("Favorite Cities", fontSize = 24.sp)
        }

        if (cityList.isEmpty()) {
            item {
                Text("No favorite cities yet")
            }
        } else {
            items(cityList.size) { index ->
                CityCard(
                    city = cityList[index],
                    onClick = {
                        prefs.setCurrentCity(cityList[index])
                        WeatherApiParams.city = cityList[index]
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

@Composable
fun CityCard(city: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = city.replaceFirstChar { it.uppercase() }, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun ExtraInformationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Extra info", fontSize = 32.sp)
        Text("dane dodatkowe np.: informacje o sile i kierunku wiatru, wilgotności widoczności", fontSize = 20.sp)
    }
}

@Composable
fun WeatherScreen(modifier: Modifier = Modifier) {
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var isFavorite by rememberSaveable { mutableStateOf(false)}

    var isOffline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isFavorite = prefs.getCityList().contains(WeatherApiParams.city.lowercase())
            weather = RetrofitClient.api.getWeatherByCity()
            weather?.let { prefs.saveWeatherData(WeatherApiParams.city, it) }

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error: ${e.localizedMessage}"
            val savedWeather = prefs.getLastSavedWeather()
            if (savedWeather != null) {
                weather = savedWeather
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
            isLoading -> {
                CircularProgressIndicator()
            }
            errorMessage != null && weather == null -> {
                Text("Can't load data.")
                if (errorMessage!!.contains("404")) {
                    Text("City " + WeatherApiParams.city + " not found")
                }
                if (errorMessage!!.contains("Unable to resolve host")) {
                    Text("Offline mode and no saved data of " + WeatherApiParams.city)
                }
                else {
                    Text(errorMessage ?: "")
                }
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
                                    prefs.addCity(WeatherApiParams.city)
                                    weather?.let { prefs.saveWeatherData(WeatherApiParams.city, it) }
                                } else {
                                    prefs.removeCity(WeatherApiParams.city)
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


@Composable
fun ForecastScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
        WeatherScreen()
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


