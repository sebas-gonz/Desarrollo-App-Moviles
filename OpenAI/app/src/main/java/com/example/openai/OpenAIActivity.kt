package com.example.openai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyectobase.ui.OpenAIViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class OpenAIActivity : AppCompatActivity() {
    private val viewModel: OpenAIViewModel by viewModels()
    private lateinit var mensaje: EditText

    private val speechRecognitionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    results[0]
                }
            mensaje.setText(spokenText)
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


        mensaje = findViewById(R.id.ed_mensaje)
        val enviar: Button = findViewById(R.id.btn_enviar)
        val respuesta: TextView = findViewById(R.id.tx_respuesta)
        val barra_progreso: ProgressBar = findViewById(R.id.pg_progeso)
        val Microfono_pregunta: Button = findViewById(R.id.Microfono)

        enviar.setOnClickListener {
            val mensaje_texto =  mensaje.text.toString()
            if(mensaje_texto.isNotBlank()){
                viewModel.enviarMensaje(mensaje_texto)
                mensaje.text.clear()
            }
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
            viewModel.response.collectLatest { reply ->
                respuesta.text = reply
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
}