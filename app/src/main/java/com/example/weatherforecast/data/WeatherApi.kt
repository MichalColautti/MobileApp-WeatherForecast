package com.example.weatherforecast.data

import retrofit2.http.GET
import retrofit2.http.Query

object WeatherApiParams{
    val apiKey: String = "d935a7419da6eb564f3b108aff9771de"
    var units: String = "metric"
    var lang: String = "pl"
}

interface WeatherApi {
//    @GET("weather")
//    suspend fun getWeatherByCity(
//        @Query("q") city: String = WeatherApiParams.city,
//        @Query("appid") apiKey: String = WeatherApiParams.apiKey,
//        @Query("units") units: String = WeatherApiParams.units,
//        @Query("lang") lang: String = WeatherApiParams.lang
//    ): WeatherResponse

//    @GET("forecast")
//    suspend fun getForecastByCity(
//        @Query("q") city: String = WeatherApiParams.city,
//        @Query("appid") apiKey: String = WeatherApiParams.apiKey,
//        @Query("units") units: String = WeatherApiParams.units,
//        @Query("lang") lang: String = WeatherApiParams.lang
//    ): ForecastResponse

    @GET("https://api.openweathermap.org/geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String = WeatherApiParams.apiKey
    ): List<SearchResult>

    @GET("weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = WeatherApiParams.apiKey,
        @Query("units") units: String = WeatherApiParams.units,
        @Query("lang") lang: String = WeatherApiParams.lang
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecastByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = WeatherApiParams.apiKey,
        @Query("units") units: String = WeatherApiParams.units,
        @Query("lang") lang: String = WeatherApiParams.lang
    ): ForecastResponse

}