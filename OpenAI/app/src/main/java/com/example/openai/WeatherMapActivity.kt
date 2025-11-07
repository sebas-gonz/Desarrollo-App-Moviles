package com.example.openai
import android.view.LayoutInflater
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.android.gms.maps.model.UrlTileProvider
import java.net.MalformedURLException
import java.net.URL
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.openai.api.WeatherApiClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.openai.BuildConfig

class WeatherMapActivity : AppCompatActivity(), com.google.android.gms.maps.OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val api_weather_key = BuildConfig.OWM_API_KEY

    private var tempMarker: Marker? = null

    private val weatherApi = WeatherApiClient.api
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {

                activarMiUbicacion()
            } else {

                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_weather_map)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val vistaGlobal = LatLng(20.0, 0.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vistaGlobal, 2f))

        activarMiUbicacion()

        addWeatherTileOverlay("temp_new")
        mMap.setOnCameraIdleListener {
            val zoom = mMap.cameraPosition.zoom
            if (zoom < 6.0f) {
                tempMarker?.remove()
                tempMarker = null
            } else {
                // Si el zoom es bueno, obtenemos el centro del mapa
                val centerLatLng = mMap.cameraPosition.target
                // Llamamos a la nueva función
                getWeatherForMapCenter(centerLatLng)
            }
        }
    }
    private fun activarMiUbicacion() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

                mMap.isMyLocationEnabled = true
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {

                Toast.makeText(this, "Necesitamos tu ubicación para mostrarla en el mapa", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {

                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    private fun addWeatherTileOverlay(capa: String) {
        val tileProvider: TileProvider = object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                val urlStr = String.format(
                    "https://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=%s",
                    capa, zoom, x, y, api_weather_key
                )
                return try { URL(urlStr) } catch (e: MalformedURLException) { null }
            }
        }
        mMap.addTileOverlay(
            TileOverlayOptions()
                .tileProvider(tileProvider)
                .transparency(0.0f)
        )
    }
    private fun getWeatherForMapCenter(location: LatLng) {
        lifecycleScope.launch {
            try {
                // 1. Llamar a la NUEVA API
                val response = weatherApi.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    appid = api_weather_key
                )

                // 2. Borrar marcador antiguo
                tempMarker?.remove()

                // 3. Crear marcador nuevo
                val temp = response.main.temp.roundToInt()
                val position = LatLng(response.coord.lat, response.coord.lon)
                val markerIcon = createCustomMarker(this@WeatherMapActivity, "$temp°C")

                tempMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .icon(markerIcon)
                        .anchor(0.5f, 1.0f)
                        .title("${response.name}: $temp°C")
                )

            } catch (e: Exception) {
                Log.e("WeatherMapActivity", "Error al obtener clima: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@WeatherMapActivity, "Error API: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun createCustomMarker(context: Context, text: String): BitmapDescriptor {
        val markerView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.marker_temperatura, null)

        val tvTemp = markerView.findViewById<TextView>(R.id.tv_temp)
        tvTemp.text = text

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}