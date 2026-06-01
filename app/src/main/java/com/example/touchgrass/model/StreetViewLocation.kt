package com.example.touchgrass.model

import com.google.android.gms.maps.model.LatLng

data class StreetViewLocation(
    val coordinates: LatLng,
    val countryName: String = "Inconnu",
    val southwest: LatLng? = null,
    val northeast: LatLng? = null
)
