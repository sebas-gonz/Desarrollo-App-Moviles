package com.example.openai.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.openai.db.CiudadContract.CityEntry
import android.provider.BaseColumns

class CiudadDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "WeatherApp.db"
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${CityEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${CityEntry.COLUMN_NAME} TEXT UNIQUE," +
                    "${CityEntry.COLUMN_TEMP} REAL," +
                    "${CityEntry.COLUMN_HUMIDITY} INTEGER," +
                    "${CityEntry.COLUMN_WIND_SPEED} REAL," +
                    "${CityEntry.COLUMN_LAT} REAL," +
                    "${CityEntry.COLUMN_LON} REAL)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${CityEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}