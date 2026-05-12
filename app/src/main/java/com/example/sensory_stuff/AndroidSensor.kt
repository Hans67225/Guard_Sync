package com.example.sensory_stuff

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import android.util.Log

abstract class AndroidSensor(
    private val context: Context,
    private val sensorFeature: String,
    sensorType: Int
) : MeasurableSensor(sensorType), SensorEventListener {

    override val doesSensorExist: Boolean
        get() = context.packageManager.hasSystemFeature(sensorFeature)

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    override fun startListening()
    {
        Log.d("SENSOR_DEBUG", "startListening called for type=$sensorType")
        if(!doesSensorExist)
        {
            Log.d("SENSOR_DEBUG", "Sensor feature NOT available: $sensorFeature")
            return
        }
        if(!::sensorManager.isInitialized && sensor == null )
        {
            sensorManager = context.getSystemService(SensorManager::class.java) as SensorManager
            sensor = sensorManager.getDefaultSensor(sensorType)

            Log.d("SENSOR_DEBUG", "Sensor object: $sensor")
        }

        sensor?.let {
            Log.d("SENSOR_DEBUG", "Registering listener for type=$sensorType")
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            Log.d("SENSOR_DEBUG", "Sensor is NULL for type=$sensorType")
        }
    }

    override fun stopListening() {

        Log.d("SENSOR_DEBUG", "stopListening called for type=$sensorType")
        if(!doesSensorExist || !::sensorManager.isInitialized)
        {
            return
        }
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(!doesSensorExist)
        {
            return
        }

        if (event == null) {
            Log.d("SENSOR_DEBUG", "Event is NULL")
            return
        }

        if(event.sensor.type == sensorType){

            Log.d("SENSOR_DEBUG", "onSensorChanged triggered for type=$sensorType")
            Log.d("SENSOR_DEBUG", "RAW values: ${event.values.joinToString()}")


            onSensorValuesChanged?.invoke(event.values.toList())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
}