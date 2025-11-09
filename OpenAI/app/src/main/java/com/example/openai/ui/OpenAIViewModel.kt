package com.example.proyectobase.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openai.BuildConfig
import com.example.openai.api.OpenAIClient
import com.example.openai.api.WeatherApi
import com.example.openai.api.WeatherApiClient
import com.example.openai.db.CiudadDbManager
import com.example.openai.model.CityWeather
import com.example.proyectobase.repository.OpenAIRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OpenAIViewModel( application: Application,
    private val repository: OpenAIRepository = OpenAIRepository(OpenAIClient.api),
    private val weatherApi: WeatherApi = WeatherApiClient.api,
    private val owmApiKey: String = BuildConfig.OWM_API_KEY
) : AndroidViewModel(application) {
    private val _response = MutableStateFlow<String>("")
    private val _navigateToMap = MutableStateFlow<LatLng?>(null)
    val navigateToMap: StateFlow<LatLng?> = _navigateToMap
    val response: StateFlow<String> = _response
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _cityWeatherList = MutableStateFlow<List<CityWeather>>(emptyList())
    val cityWeatherList: StateFlow<List<CityWeather>> = _cityWeatherList

    private val ciudadManager = CiudadDbManager(application)

    init {
        cargarClimaCiudades()
    }
    private fun cargarClimaCiudades() {

        viewModelScope.launch { //
            try {
                var ciudadesDb = ciudadManager.getCities()

                if (ciudadesDb.isEmpty()) {
                    ciudadManager.checkAndAddDefaultCities { cityName ->
                        try {
                            weatherApi.getWeatherByCityName(cityName, owmApiKey)
                        } catch (e: Exception) { null }
                    }
                    // Vuelve a leer de la BD
                    ciudadesDb = ciudadManager.getCities()
                }
                _cityWeatherList.value = ciudadesDb

                for (city in ciudadesDb) {
                    try {
                        val datos = weatherApi.getWeatherByCityName(city.name, owmApiKey)
                        // Guarda los datos en la BD
                        ciudadManager.saveCity(datos)
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Error al refrescar ${city.name}: ${e.message}")
                    }
                }
                _cityWeatherList.value = ciudadManager.getCities()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error cargando lista de ciudades: ${e.message}")
            }
        }
    }
    fun addNewCity(cityName: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val nuevaCiudadWeather = weatherApi.getWeatherByCityName(cityName, owmApiKey)

                ciudadManager.saveCity(nuevaCiudadWeather)

                _response.value = "${nuevaCiudadWeather.name} añadida."

                cargarClimaCiudades()

            } catch (e: Exception) {
                _response.value = "Error: No se pudo encontrar la ciudad '$cityName'"
            } finally {
                _loading.value = false
            }
        }
    }
    private val _speakText = MutableStateFlow<String?>(null)
    val speakText: StateFlow<String?> = _speakText
    fun enviarMensaje(userText: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val reply = repository.getCompletion(userText)
                _response.value = reply
            } catch (e: Exception) {
                _response.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    fun procesarComandoVoz(prompt: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _response.value = "Analizando..."

                // clasifica la intención
                val intencion = repository.analizarIntencion(prompt)

                // decidir qué hacer basado en la intención
                when (intencion.intencion) {

                    "ver_mapa" -> {
                        if (intencion.ciudad.equals("None", ignoreCase = true)) {
                            throw Exception("No entendí la ciudad para mostrar en el mapa.")
                        }
                        _response.value = "OK, mostrando ${intencion.ciudad} en el mapa..."
                        // Lógica de 'procesarPreguntaClima'
                        val weatherData = weatherApi.getWeatherByCityName(intencion.ciudad, owmApiKey)
                        _navigateToMap.value = LatLng(weatherData.coord.lat, weatherData.coord.lon)
                    }

                    "escuchar_pronostico" -> {
                        if (intencion.ciudad.equals("None", ignoreCase = true)) {
                            throw Exception("No entendí la ciudad para el pronóstico.")
                        }
                        _response.value = "OK, buscando pronóstico para ${intencion.ciudad}..."
                        // Lógica de 'procesarPreguntaPronostico'
                        val forecastData = weatherApi.getForecast(intencion.ciudad, owmApiKey)
                        val summary = repository.getForecastSummary(prompt, forecastData)
                        _response.value = summary
                        _speakText.value = summary
                    }

                    else -> { // error
                        _response.value = "No entendí la pregunta sobre el clima."
                        _speakText.value = "No entendí tu pregunta sobre el clima."
                    }
                }

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                _response.value = errorMsg
                _speakText.value = errorMsg
            } finally {
                _loading.value = false
            }
        }
    }

    fun getForecastForCity(cityName: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _response.value = "Buscando pronóstico para $cityName..." //

                // promp para pedir el pronostico
                val prompt = "Dame un resumen del pronóstico de 5 días para $cityName"

                // datos del pronostico (forecast)
                val forecastData = weatherApi.getForecast(cityName, owmApiKey) //

                // Ia lo resume
                val summary = repository.getForecastSummary(prompt, forecastData) //

                // se muestra la informacion en el textview
                _response.value = summary

                //Opcional: se puede agregar la respuesta por chat de voz

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                _response.value = errorMsg
            } finally {
                _loading.value = false
            }
        }
    }
    fun navigationHandled() {
        _navigateToMap.value = null
    }
    fun speechHandled() {
        _speakText.value = null
    }


}