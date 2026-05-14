package com.example.guard_sync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guard_sync.ui.theme.Guard_SyncTheme
import dagger.hilt.android.AndroidEntryPoint

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
//import com.example.guard_sync.geo_locn.LocationCallback
import com.example.guard_sync.geo_locn.LocationService
//import com.example.guard_sync.sensors.MainViewModel

import com.example.guard_sync.sensors.*
import java.util.jar.Manifest

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

import androidx.navigation.compose.*


import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.guard_sync.geo_locn.LocationHolder
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapLibre.getInstance(
            this,
            null,
            WellKnownTileServer.MapLibre
        )

        ActivityCompat.requestPermissions(
            this,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            },
            0
        )

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Log.d("CHECK", "Rotation: ${sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)}")
        Log.d("CHECK", "Gyro: ${sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)}")
        Log.d("CHECK", "Temp: ${sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)}")

        setContent {

            //val navController = rememberNavController()
            Guard_SyncTheme {


                Guard_SyncTheme {
                    val viewModel = viewModel<MainViewModel>()
                    val isDark = viewModel.isDark
                    val x = viewModel.x
                    val y = viewModel.y
                    val z = viewModel.z
                    val azu = viewModel.azu
                    val pitch = viewModel.pitch
                    val roll = viewModel.roll

                    var showMap by remember { mutableStateOf(true) }
                    var mapRef: MapLibreMap? = null
                    val location by LocationHolder.location.collectAsState()



                    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

                    NavigationSuiteScaffold(
                        navigationSuiteItems = {
                            AppDestinations.entries.forEach {
                                item(
                                    icon = {
                                        Icon(
                                            painterResource(it.icon),
                                            contentDescription = it.label
                                        )
                                    },
                                    label = { Text(it.label) },
                                    selected = it == currentDestination,
                                    onClick = { currentDestination = it }
                                )
                            }
                        }
                    ) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            when (currentDestination) {

                                AppDestinations.HOME -> Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                        .background(if (isDark) Color.DarkGray else Color.White),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "\nX: $x \nY: $y \nZ: $z\nAzimuthal: $azu°\nPitch: $pitch°\nRoll: $roll°",
                                        color = if (isDark) Color.White else Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        Intent(
                                            applicationContext,
                                            LocationService::class.java
                                        ).apply {
                                            action = LocationService.ACTION_START
                                            startService(this)
                                        }
                                    }) {
                                        Text(text = "Start Tracking")
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        Intent(
                                            applicationContext,
                                            LocationService::class.java
                                        ).apply {
                                            action = LocationService.ACTION_STOP
                                            startService(this)
                                        }
                                    }) {
                                        Text(text = "Stop Tracking")
                                    }
                                }

                                AppDestinations.Map -> Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    if (showMap) {
                                        AndroidView(
                                            factory = { context ->
                                                MapView(context).apply {
                                                    getMapAsync { map ->
                                                        map.setStyle("https://demotiles.maplibre.org/style.json")
                                                        location?.let {
                                                            val lat = it.first
                                                            val long = it.second

                                                            val point = LatLng(lat, long)

                                                            map.clear()
                                                            map.addMarker(MarkerOptions().position(point))
                                                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0))
                                                        }


                                                    }
                                                }
                                            },



                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                AppDestinations.PROFILE -> Text(
                                    text = "Your Profile",
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }

        }
    }



    enum class AppDestinations(
        val label: String,
        val icon: Int,
    ) {
        HOME("Home", R.drawable.ic_home),
        Map("Map", R.drawable.ic_map),
        PROFILE("Profile", R.drawable.ic_account_box),
    }
}