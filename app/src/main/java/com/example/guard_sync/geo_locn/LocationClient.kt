package com.example.guard_sync.geo_locn

import android.location.Location
import androidx.compose.ui.graphics.Interval
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long) : Flow<Location>

    class LocationException(message: String) : Exception()
}