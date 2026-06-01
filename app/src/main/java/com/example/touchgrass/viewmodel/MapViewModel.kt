package com.example.touchgrass.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

class MapViewModel : ViewModel() {
    private val _guessedLocation = MutableStateFlow<LatLng?>(null)
    val guessedLocation: StateFlow<LatLng?> = _guessedLocation.asStateFlow()

    fun updateGuessedLocation(location: LatLng) {
        _guessedLocation.value = location
    }

    fun reset() {
        _guessedLocation.value = null
    }

    fun calculateDistance(target: LatLng, guess: LatLng): Double {
        val r = 6371.0 // Rayon de la Terre en km
        val dLat = Math.toRadians(guess.latitude - target.latitude)
        val dLon = Math.toRadians(guess.longitude - target.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(target.latitude)) * cos(Math.toRadians(guess.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
