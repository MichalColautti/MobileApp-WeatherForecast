package com.example.weatherforecast.data

data class WeatherResponse(
    val name: String,
    val coord: Coord,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val visibility: Int
)

data class Coord(
    val lon: Double, val lat: Double
)

data class Main(
    val temp: Double,
    val pressure: Double,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt_txt: String,
    val main: ForecastMain,
    val weather: List<Weather>
)

data class ForecastMain(
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Double,
    val humidity: Int
)

data class SearchResult(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)

