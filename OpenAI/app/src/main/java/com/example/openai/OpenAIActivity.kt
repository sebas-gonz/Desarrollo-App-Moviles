package com.example.openai

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyectobase.ui.OpenAIViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OpenAIActivity : AppCompatActivity() {
    private val viewModel: OpenAIViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_aiactivity)


        val mensaje: EditText = findViewById(R.id.ed_mensaje)
        val enviar: Button = findViewById(R.id.btn_enviar)
        val respuesta: TextView = findViewById(R.id.tx_respuesta)
        val barra_progreso: ProgressBar = findViewById(R.id.pg_progeso)

        enviar.setOnClickListener {
            val mensaje_texto =  mensaje.text.toString()
            if(mensaje_texto.isNotBlank()){
                viewModel.enviarMensaje(mensaje_texto)
                mensaje.text.clear()
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
}