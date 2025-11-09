package com.example.openai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.openai.model.CityWeather
import kotlin.math.roundToInt

class CityWeatherAdapter(
    private val onClick: (CityWeather) -> Unit
) : ListAdapter<CityWeather, CityWeatherAdapter.CityViewHolder>(CityDiffCallback) {

    class CityViewHolder(view: View, val onClick: (CityWeather) -> Unit) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tv_city_name)
        private val tvTemp: TextView = view.findViewById(R.id.tv_temperature)
        private val tvHumidity: TextView = view.findViewById(R.id.tv_humidity)
        private val tvWind: TextView = view.findViewById(R.id.tv_wind_speed)
        private var currentCity: CityWeather? = null

        init {
            view.setOnClickListener {
                currentCity?.let {
                    onClick(it)
                }
            }
        }

        fun bind(city: CityWeather) {
            currentCity = city
            tvName.text = city.name
            tvTemp.text = "${city.main.temp.roundToInt()}°C"
            tvHumidity.text = "Humedad: ${city.main.humidity}%"
            tvWind.text = "Viento: ${city.wind.speed.roundToInt()} km/h"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_city, parent, false)
        return CityViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = getItem(position)
        holder.bind(city)
    }
}

object CityDiffCallback : DiffUtil.ItemCallback<CityWeather>() {
    override fun areItemsTheSame(oldItem: CityWeather, newItem: CityWeather): Boolean {
        return oldItem.name == newItem.name
    }
    override fun areContentsTheSame(oldItem: CityWeather, newItem: CityWeather): Boolean {
        return oldItem == newItem
    }
}