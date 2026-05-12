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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.guard_sync.geo_locn.LocationService
//import com.example.guard_sync.sensors.MainViewModel

import com.example.guard_sync.sensors.*
import java.util.jar.Manifest


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            Guard_SyncTheme{
                val viewModel = viewModel<MainViewModel>()
                val isDark = viewModel.isDark
                val x = viewModel.x
                val y = viewModel.y
                val z = viewModel.z
                val t = viewModel.t
                val azu = viewModel.azu
                val pitch = viewModel.pitch
                val roll = viewModel.roll
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if(isDark) Color.DarkGray else Color.White
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "\nX: $x \nY: $y \nZ: $z\nAzimuthal: $azu°\nPitch: $pitch°\nRoll: $roll°",
                        color = if(isDark) Color.White else Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        Intent(applicationContext, LocationService::class.java).apply {
                            action = LocationService.ACTION_START
                            startService(this)
                        }
                    }) {
                        Text(text = "Start Tracking")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        Intent(applicationContext, LocationService::class.java).apply {
                            action = LocationService.ACTION_STOP
                            startService(this)
                        }
                    }) {
                        Text(text = "Stop Tracking")
                    }
                }






         //   BackgroundLocationTrackingTheme {}
            }
        }
    }
}