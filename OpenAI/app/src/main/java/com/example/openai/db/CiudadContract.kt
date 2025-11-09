package com.example.openai.db

import android.provider.BaseColumns

object CiudadContract {
    object CityEntry : BaseColumns {
        const val TABLE_NAME = "cities"

        const val COLUMN_NAME = "name"
        const val COLUMN_TEMP = "temperature"
        const val COLUMN_HUMIDITY = "humidity"
        const val COLUMN_WIND_SPEED = "wind_speed"
        const val COLUMN_LAT = "latitude"
        const val COLUMN_LON = "longitude"
    }
}