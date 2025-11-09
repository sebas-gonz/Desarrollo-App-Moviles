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
import android.content.Intent
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WeatherMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val api_weather_key = BuildConfig.OWM_API_KEY
    private val weatherApi = WeatherApiClient.api
    private var tempMarker: Marker? = null
    private var currentTileOverlay: TileOverlay? = null // <-- Para guardar la capa actual
    private var currentLayerType = "temp"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                activarMiUbicacion()
                zoomToCurrentLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 2f))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_weather_map)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val fabChat: FloatingActionButton = findViewById(R.id.fab_open_chat)
        fabChat.setOnClickListener {
            val intent = Intent(this, OpenAIActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

            startActivity(intent)
        }
        val toggleGroup: MaterialButtonToggleGroup = findViewById(R.id.toggle_button_group)
        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                // Borra la capa de color anterior
                currentTileOverlay?.remove()

                // Actualiza el tipo de capa y añade la nueva
                when (checkedId) {
                    R.id.button_temp -> {
                        currentLayerType = "temp"
                        addWeatherTileOverlay("temp_new") // Capa de temperatura
                    }
                    R.id.button_wind -> {
                        currentLayerType = "wind"
                        addWeatherTileOverlay("wind_new") // Capa de viento
                    }
                    R.id.button_humidity -> {
                        currentLayerType = "humidity"
                    }
                }

                // Refresca el marcador central con la nueva data
                refreshMapCenterMarker()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        activarMiUbicacion()

        if (intent.hasExtra("EXTRA_LAT")) {
            // Centra en la ciudad que preguntó la IA
            val lat = intent.getDoubleExtra("EXTRA_LAT", 0.0)
            val lon = intent.getDoubleExtra("EXTRA_LON", 0.0)
            val initialLocation = LatLng(lat, lon)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))
        } else {
            zoomToCurrentLocation()
        }
        addWeatherTileOverlay("temp_new")
        mMap.setOnCameraIdleListener {
            refreshMapCenterMarker()
        }
    }
    private fun refreshMapCenterMarker() {
        val zoom = mMap.cameraPosition.zoom
        if (zoom < 6.0f) {
            tempMarker?.remove()
            tempMarker = null
        } else {
            val centerLatLng = mMap.cameraPosition.target
            getWeatherForMapCenter(centerLatLng)
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
        currentTileOverlay = mMap.addTileOverlay(
            TileOverlayOptions()
                .tileProvider(tileProvider)
                .transparency(0.3f) // Opacidad
        )
    }
    private fun getWeatherForMapCenter(location: LatLng) {
        lifecycleScope.launch {
            try {
                val response = weatherApi.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    appid = api_weather_key
                )
                tempMarker?.remove()

                val (markerText, markerTitle) = when (currentLayerType) {
                    "temp" -> {
                        val value = response.main.temp.roundToInt()
                        Pair("$value°C", "${response.name}: $value°C")
                    }
                    "wind" -> {
                        val value = response.wind.speed.roundToInt() // Viento
                        Pair("$value km/h", "${response.name} Viento: $value km/h")
                    }
                    "humidity" -> {
                        val value = response.main.humidity // Humedad
                        Pair("$value%", "${response.name} Humedad: $value%")
                    }
                    else -> Pair("", response.name) // Defecto: temperatura
                }
                val position = LatLng(response.coord.lat, response.coord.lon)
                val markerIcon = createCustomMarker(this@WeatherMapActivity, markerText)

                tempMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .icon(markerIcon)
                        .anchor(0.5f, 1.0f)
                        .title(markerTitle)
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
    private fun zoomToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 2f))
                        Toast.makeText(this, "Activa el GPS para centrar el mapa", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}