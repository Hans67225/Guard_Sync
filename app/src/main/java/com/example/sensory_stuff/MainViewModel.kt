package com.example.sensory_stuff

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.hardware.SensorManager
import androidx.core.graphics.rotationMatrix

@HiltViewModel
class MainViewModel @Inject constructor(
    //private val lightSensor: MeasurableSensor
    @LightSensorQualifier private val lightSensor: MeasurableSensor,
    @AccelerometerQualifier private val accelerometerSensor: MeasurableSensor,
    @TempSensorQualifier private val tempSensor: MeasurableSensor,
    @HeadingSensorQualifier private val headingSensor: MeasurableSensor
) : ViewModel() {

    var isDark by mutableStateOf(false)
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    var z by mutableStateOf(0f)

    var t by mutableStateOf(0f)

    var azu by mutableStateOf(0f)
    var pitch by mutableStateOf(0f)
    var roll by mutableStateOf(0f)




    init {
        lightSensor.startListening()
        lightSensor.setOnSensorValuesChangedListener { values ->
            val lux = values[0]
            isDark = lux < 60f
        }

        accelerometerSensor.startListening()
        accelerometerSensor.setOnSensorValuesChangedListener { values ->
            x = values[0]
            y = values[1]
            z = values[2]
        }

        headingSensor.startListening()
        headingSensor.setOnSensorValuesChangedListener { values ->
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)

            SensorManager.getRotationMatrixFromVector(
                rotationMatrix,
                values.toFloatArray()
            )

            SensorManager.getOrientation(rotationMatrix, orientation)

            azu = Math.toDegrees(orientation[0].toDouble()).toFloat()
            pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
        }

        tempSensor.startListening()
        tempSensor.setOnSensorValuesChangedListener { values ->
            t = values[0]
        }
    }
}