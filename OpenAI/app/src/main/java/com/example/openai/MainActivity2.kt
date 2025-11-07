package com.example.openai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        //ACTIVITY DESTINO
        val msjeBienvenida:TextView = findViewById(R.id.tx_bienvenido)
        //creo variable asigno valor recibido desde otro activity
        val usuarioDesdeOtroActivity = intent.getStringExtra("sesion")
        //seteo un TextView reemplazando el texto por el contenido.
        msjeBienvenida.text = usuarioDesdeOtroActivity.toString()
        val contrasenia: TextView = findViewById(R.id.tx_contrasenia)
        contrasenia.text = intent.getStringExtra("par_contrasena").toString()
        val recibeContrasena = intent.getStringExtra("par_contrasena")

        val btn_api: Button = findViewById(R.id.btn_api)
        btn_api.setOnClickListener {
            val nuevaVentana = Intent(this, OpenAIActivity::class.java)
            startActivity(nuevaVentana)
        }

        val btn_mapa: Button = findViewById(R.id.btn_api_clima)
        btn_mapa.setOnClickListener {
            val intentMapa = Intent(this, WeatherMapActivity::class.java)
            startActivity(intentMapa)
        }

        val duracion = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this, "bienvenido $usuarioDesdeOtroActivity", duracion)
        toast.show()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
