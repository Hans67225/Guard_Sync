package com.example.guard_sync.sensors

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.guard_sync.geo_locn.LocationService
import kotlin.math.abs
import kotlin.math.round
import dagger.hilt.android.internal.Contexts.getApplication
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.application
import com.example.guard_sync.R

@SuppressLint("MissingPermission")
fun sendIdleNotification(context: Context) {
    val channelId = "idle_alert_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Idle Alert",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when you've been idle too long"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_map)
        .setContentTitle("ALERT")
        .setContentText("GET MOVIN")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(1001, notification)
}
@HiltViewModel
class MainViewModel @Inject constructor(
    private val application : Application,
    //private val lightSensor: MeasurableSensor
    @LightSensorQualifier private val lightSensor: MeasurableSensor,
    @AccelerometerQualifier private val accelerometerSensor: MeasurableSensor,
    @HeadingSensorQualifier private val headingSensor: MeasurableSensor
) : AndroidViewModel(application) {

    var isDark by mutableStateOf(false)
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    var z by mutableStateOf(0f)

    var azu by mutableStateOf(0f)
    var pitch by mutableStateOf(0f)
    var roll by mutableStateOf(0f)

    var c1 by  mutableStateOf(0f)
    var c2 by  mutableStateOf(0f)
    var c3 by  mutableStateOf(0f)

    var panikSince = 0L
    var movingSince = 0L
    var idleSince = 0L
    var state by  mutableStateOf("Idle")



    init {
        lightSensor.startListening()
        lightSensor.setOnSensorValuesChangedListener { values ->
            val lux = values[0]
            isDark = lux < 60f
        }

        accelerometerSensor.startListening()
        accelerometerSensor.setOnSensorValuesChangedListener { values ->

            val newX = round(values[0] * 10) / 10
            val newY = round(values[1] * 10) / 10
            val newZ = round(values[2] * 10) / 10

            val dx = abs(x - newX)
            val dy = abs(y - newY)
            val dz = abs(z - newZ)

            x = newX
            y = newY
            z = newZ

            val isPanik  = dx > 4.2 || dy > 4.2 || dz > 4.2
            val isMoving = !isPanik && (dx in 0.2..4.2 || dy in 0.2..4.2 || dz in 0.2..4.2)

            val now = System.currentTimeMillis()

            when {
                isPanik  -> {
                    if (panikSince == 0L) panikSince = now
                    movingSince = 0L
                    idleSince = 0L
                    if (now - panikSince >= 2_000) state = "Panik"
                }
                isMoving -> {
                    if (movingSince == 0L) movingSince = now
                    panikSince = 0L
                    idleSince = 0L
                    if (now - movingSince >= 1_000) state = "Moving"
                }
                else -> {
                    if (idleSince == 0L) idleSince = now
                    panikSince = 0L
                    movingSince = 0L
                    if (now - idleSince >= 5_000) state = "Idle"
                    if(now - idleSince >= 60_000) {
                        if (state != "Mov Bruv") {
                            sendIdleNotification(application)
                        }
                        state = "Mov Bruv"
                    }
                }
            }
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

            azu = round(Math.toDegrees(orientation[0].toDouble()).toFloat()*10)/10
            pitch = round(Math.toDegrees(orientation[1].toDouble()).toFloat()*10)/10
            roll = round(Math.toDegrees(orientation[2].toDouble()).toFloat()*10)/10
        }


    }
}