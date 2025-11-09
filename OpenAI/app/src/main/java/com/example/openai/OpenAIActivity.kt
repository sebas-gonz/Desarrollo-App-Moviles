package com.example.openai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.openai.ui.OpenAIViewModelFactory
import com.example.proyectobase.ui.OpenAIViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class OpenAIActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val viewModel: OpenAIViewModel by viewModels {
        OpenAIViewModelFactory(application)
    }
    private lateinit var mensaje: EditText
    private lateinit var tts: TextToSpeech

    private lateinit var cityWeatherAdapter: CityWeatherAdapter
    private val speechRecognitionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    results[0]
                }
            if (spokenText != null) {

                viewModel.procesarComandoVoz(spokenText)
            }

        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                lanzarReconocimientoDeVoz()
            } else {
                Toast.makeText(this, "El permiso para usar el micrófono es necesario", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_aiactivity)

        tts = TextToSpeech(this, this)

        mensaje = findViewById(R.id.ed_mensaje)
        val enviar: Button = findViewById(R.id.btn_enviar)
        val respuesta: TextView = findViewById(R.id.tx_respuesta)
        val barra_progreso: ProgressBar = findViewById(R.id.pg_progeso)
        val Microfono_pregunta: Button = findViewById(R.id.Microfono)
        val btnAddCity: ImageButton = findViewById(R.id.btn_add_city)
        val btnVerMapa: Button = findViewById(R.id.btn_ver_mapa)
        setupCityWeatherList()

        enviar.setOnClickListener {
            val mensaje_texto =  mensaje.text.toString()
            if(mensaje_texto.isNotBlank()){
                viewModel.enviarMensaje(mensaje_texto)
                mensaje.text.clear()
            }
        }

        btnVerMapa.setOnClickListener {
            val intentMapa = Intent(this, WeatherMapActivity::class.java)
            startActivity(intentMapa)
        }

        btnAddCity.setOnClickListener {
            mostrarDialogoNuevaCiudad()
        }

        Microfono_pregunta.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    lanzarReconocimientoDeVoz()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.cityWeatherList.collectLatest { cityList ->
                cityWeatherAdapter.submitList(cityList)
            }
        }

        lifecycleScope.launch {
            viewModel.response.collectLatest { reply ->
                respuesta.text = reply
            }
        }

        lifecycleScope.launch {
            viewModel.speakText.collectLatest { text ->
                if (text != null) {
                    // ¡Habla!
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    viewModel.speechHandled() // Avisa al ViewModel
                }
            }
        }
        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                barra_progreso.visibility = if (isLoading){
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
            }
        }
        lifecycleScope.launch {
            viewModel.navigateToMap.collectLatest { latLng ->
                if (latLng != null) {
                    val intent = Intent(this@OpenAIActivity, WeatherMapActivity::class.java)
                    intent.putExtra("EXTRA_LAT", latLng.latitude)
                    intent.putExtra("EXTRA_LON", latLng.longitude)
                    startActivity(intent)

                    viewModel.navigationHandled()
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Configura el idioma a español
            val result = tts.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "El idioma Español no está soportado")
                tts.setLanguage(Locale("es"))
            }
        } else {
            Log.e("TTS", "Falló la inicialización de TextToSpeech")
        }
    }

    override fun onDestroy() {
        // Apaga el motor de TTS para liberar recursos
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
    private fun lanzarReconocimientoDeVoz() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
        }
        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tu dispositivo no soporta el reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCityWeatherList() {
        val rvCityWeather: RecyclerView = findViewById(R.id.rv_city_weather)
        // Define qué hacer cuando se selecciona una ciudad
        cityWeatherAdapter = CityWeatherAdapter { city ->
            viewModel.getForecastForCity(city.name)
            mensaje.setText("")

        }
        rvCityWeather.adapter = cityWeatherAdapter
    }

    private fun mostrarDialogoNuevaCiudad() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Añadir Nueva Ciudad")

        val input = EditText(this)
        input.hint = "Nombre de la ciudad"
        builder.setView(input)

        builder.setPositiveButton("Añadir") { dialog, _ ->
            val cityName = input.text.toString()
            if (cityName.isNotBlank()) {
                viewModel.addNewCity(cityName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}