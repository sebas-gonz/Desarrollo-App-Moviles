package com.example.openai.db

import android.content.ContentValues
import android.content.Context
import com.example.openai.model.CityWeather
import com.example.openai.model.Coord
import com.example.openai.model.MainTemp
import com.example.openai.model.Wind

class CiudadDbManager(context: Context) {

    private val dbHelper = CiudadDbHelper(context)

    fun saveCity(city: CityWeather) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(CiudadContract.CityEntry.COLUMN_NAME, city.name)
            put(CiudadContract.CityEntry.COLUMN_TEMP, city.main.temp)
            put(CiudadContract.CityEntry.COLUMN_HUMIDITY, city.main.humidity)
            put(CiudadContract.CityEntry.COLUMN_WIND_SPEED, city.wind.speed)
            put(CiudadContract.CityEntry.COLUMN_LAT, city.coord.lat)
            put(CiudadContract.CityEntry.COLUMN_LON, city.coord.lon)
        }

        // Actualizar la fila si la ciudad tiene el mismo nombre
        val rowsAffected = db.update(
            CiudadContract.CityEntry.TABLE_NAME,
            values,
            "${CiudadContract.CityEntry.COLUMN_NAME} = ?",
            arrayOf(city.name)
        )

        if (rowsAffected == 0) {
            db.insert(CiudadContract.CityEntry.TABLE_NAME, null, values)
        }
    }

    fun getCities(): List<CityWeather> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            CiudadContract.CityEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val cities = mutableListOf<CityWeather>()
        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(CiudadContract.CityEntry.COLUMN_NAME))
                val temp = getDouble(getColumnIndexOrThrow(CiudadContract.CityEntry.COLUMN_TEMP))
                val humidity = getInt(getColumnIndexOrThrow(CiudadContract.CityEntry.COLUMN_HUMIDITY))
                val windSpeed = getDouble(getColumnIndexOrThrow(CiudadContract.CityEntry.COLUMN_WIND_SPEED))
                val lat = getDouble(getColumnIndexOrThrow(CiudadContract.CityEntry.COLUMN_LAT))
                val lon = getDouble(getColumnIndexOrThrow(CiudadContract.CityEntry.COLUMN_LON))

                // Reconstruimos el objeto CityWeather
                cities.add(
                    CityWeather(
                        name = name,
                        coord = Coord(lat = lat, lon = lon),
                        main = MainTemp(temp = temp, humidity = humidity),
                        wind = Wind(speed = windSpeed)
                    )
                )
            }
        }
        cursor.close()
        return cities
    }

    suspend fun checkAndAddDefaultCities(apiCall: suspend (String) -> CityWeather?) {
        if (getCities().isNotEmpty()) {
            return // La BD ya tiene datos, no hacer nada
        }

        val defaultCities = listOf("Santiago", "Valparaíso", "Concepción", "La Serena", "Arica", "Temuco", "Punta Arenas", "Coyhaique", "Antofagasta", "Chillan", "Talca")
        for (cityName in defaultCities) {
            val cityWeather = apiCall(cityName)
            if (cityWeather != null) {
                saveCity(cityWeather)
            }
        }
    }

}