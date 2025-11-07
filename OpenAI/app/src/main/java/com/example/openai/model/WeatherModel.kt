package com.example.openai.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherModel(
    val list: List<CityWeather>
)

@JsonClass(generateAdapter = true)
data class CityWeather(
    val name: String,
    val coord: Coord,
    val main: MainTemp
)

// Coordenadas de la ciudad
@JsonClass(generateAdapter = true)
data class Coord(
    val lat: Double,
    val lon: Double
)

// La temperatura
@JsonClass(generateAdapter = true)
data class MainTemp(
    val temp: Double
)
