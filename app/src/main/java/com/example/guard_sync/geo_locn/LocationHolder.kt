package com.example.guard_sync.geo_locn

import kotlinx.coroutines.flow.MutableStateFlow

object LocationHolder {
    val location = MutableStateFlow<Pair<Double, Double>?>(null)
}