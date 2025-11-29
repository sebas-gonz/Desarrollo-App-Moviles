package com.example.openai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar
    private lateinit var btnGoogle: SignInButton

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {
            // Si el usuario cancela, ocultamos la carga
            mostrarCarga(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.login_progress)

        // 1. Configurar el botón oficial
        btnGoogle = findViewById(R.id.sign_in_button)
        btnGoogle.setSize(SignInButton.SIZE_WIDE)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogle.setOnClickListener {
            mostrarCarga(true)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            // si ya esta logeado ir al activity del mapa
            irAlMapa(account.displayName ?: "Usuario")
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Login Exitoso
            val nombre = account.displayName ?: "Usuario Google"
            Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()
            irAlMapa(nombre)
        } catch (e: ApiException) {
            mostrarCarga(false)
            Log.w("GoogleLogin", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Error de autenticación: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    private fun irAlMapa(nombreUsuario: String) {
        val nuevaVentana = Intent(this, WeatherMapActivity::class.java)
        nuevaVentana.putExtra("sesion", nombreUsuario)
        startActivity(nuevaVentana)
        finish() // termina esta actividad para que no se pueda regresar al login
    }


    private fun mostrarCarga(cargando: Boolean) {
        if (cargando) {
            progressBar.visibility = View.VISIBLE
            btnGoogle.visibility = View.INVISIBLE // se oculta el boton para que no se pulse 2 veces
        } else {
            progressBar.visibility = View.GONE
            btnGoogle.visibility = View.VISIBLE
        }
    }
}