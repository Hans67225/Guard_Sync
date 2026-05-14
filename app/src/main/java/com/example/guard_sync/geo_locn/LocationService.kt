package com.example.guard_sync.geo_locn

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.guard_sync.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.round

private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
private lateinit var locationClient: LocationClient

class LocationService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    val targetLat = 22.5726
    val targetLong = 88.3639
    val radius = 100.0

    fun distanceInMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            lat1, lon1, lat2, lon2, results
        )
        return results[0].toDouble()
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this,"location")
            .setContentTitle("Tracking Location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
        Log.d("LOCATION_DEBUG", "Service started")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(1000L)
            .catch { e -> e.printStackTrace() }
            
            .onEach { location ->

                Log.d("LOC_DEBUG", "Got location: $location")


                val lat = round(location.latitude * 100) / 100
                val long = round(location.longitude * 100) / 100

                LocationHolder.location.value = Pair(lat, long)

                val distance = distanceInMeters(lat, long, targetLat, targetLong)

                if (distance <= radius) {
                    Log.d("GEOFENCE", "Inside area")
                    val updatedNotification = notification.setContentText(
                        "Location: ($lat,$long), On Location"
                    )
                    notificationManager.notify(1, updatedNotification.build())

                } else {
                    Log.d("GEOFENCE", "Outside area")
                    val updatedNotification = notification.setContentText(
                        "Location: ($lat,$long), NOT on Location"
                    )
                    notificationManager.notify(1, updatedNotification.build())
                }

               // locationCallback?.onLocationUpdated(lat,long)

//                val updatedNotification = notification.setContentText(
//                    "Location: ($lat,$long)"
//                )
//                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        else {
            stopForeground(true)
        }

        stopSelf()

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}