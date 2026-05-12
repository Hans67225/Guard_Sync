package com.example.sensory_stuff

import android.R
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sensory_stuff.ui.theme.Sensory_StuffTheme
import dagger.hilt.android.AndroidEntryPoint

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Log.d("CHECK", "Rotation: ${sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)}")
        Log.d("CHECK", "Gyro: ${sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)}")
        Log.d("CHECK", "Temp: ${sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)}")

        setContent {
            Sensory_StuffTheme {
                val viewModel = viewModel<MainViewModel>()
                val isDark = viewModel.isDark
                val x = viewModel.x
                val y = viewModel.y
                val z = viewModel.z
                val t = viewModel.t
                val azu = viewModel.azu
                val pitch = viewModel.pitch
                val roll = viewModel.roll
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if(isDark) Color.DarkGray else Color.White
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if(isDark) {
                            "It's Dark Outside \nX: $x \nY: $y \nZ: $z\nAzimuthal: $azu°\nPitch: $pitch°\nRoll: $roll°\nTemp: $t°C"
                        } else{
                            "It's Bright Outside\nX: $x \nY: $y \nZ: $z\nAzimuthal: $azu°\nPitch: $pitch°\nRoll: $roll°\nTemp: $t°C"
                        },
                        color = if(isDark) Color.White else Color.DarkGray
                    )
                }

              /*  Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if(isDark) Color.DarkGray else Color.White
                        ),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = "X: $x \nY: $y \nZ: $z",
                        color = if(isDark) Color.White else Color.DarkGray
                    )
                } */
            }
        }
    }
}