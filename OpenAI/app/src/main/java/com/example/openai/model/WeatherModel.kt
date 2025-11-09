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
    val main: MainTemp,
    val wind: Wind
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
    val temp: Double,
    val humidity: Int
)

@JsonClass(generateAdapter = true)
data class Wind(
    val speed: Double
)

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    val list: List<ForecastItem> // lista de pronósticos
)

@JsonClass(generateAdapter = true)
data class ForecastItem(
    val main: MainTemp,
    val weather: List<WeatherDescription>, // descripción del clima
    val dt_txt: String // La fecha y hora
)

@JsonClass(generateAdapter = true)
data class WeatherDescription(
    val description: String
)
@JsonClass(generateAdapter = true)
data class IntencionResponse(
    val intencion: String, //
    val ciudad: String
)
