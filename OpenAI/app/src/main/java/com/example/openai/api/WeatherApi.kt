package com.example.openai.api

import com.example.openai.model.CityWeather
import com.example.openai.model.ForecastResponse
import com.example.openai.model.WeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String,
        @Query("units") units: String = "metric"
    ): CityWeather

    @GET("data/2.5/weather")
    suspend fun getWeatherByCityName(
        @Query("q") cityName: String, // Parámetro q para buscar por nombre
        @Query("appid") appid: String,
        @Query("units") units: String = "metric"
    ): CityWeather

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("appid") appid: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse

}