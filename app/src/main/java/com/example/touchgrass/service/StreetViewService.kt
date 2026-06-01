package com.example.touchgrass.service

import com.example.touchgrass.model.StreetViewLocation
import com.google.android.gms.maps.model.LatLng
import kotlin.random.Random

class StreetViewService {
    private var countrySeeds: List<StreetViewLocation> = emptyList()

    fun setSeeds(seeds: List<StreetViewLocation>) {
        this.countrySeeds = seeds
    }

    /**
     * Génère une position déterministe basée sur une graine, un round et un nombre d'essais.
     * Cela permet à plusieurs joueurs d'avoir exactement les mêmes lieux pour un même code.
     */
    fun getDeterministicLocation(gameSeed: Long, round: Int, retryCount: Int): StreetViewLocation {
        // Création d'une graine unique pour ce tirage spécifique
        val combinedSeed = gameSeed + (round * 1000) + retryCount
        val random = Random(combinedSeed)

        val seed = if (countrySeeds.isNotEmpty()) {
            countrySeeds[random.nextInt(countrySeeds.size)]
        } else {
            StreetViewLocation(LatLng(48.8584, 2.2945), "France")
        }

        val randomLatLng = if (seed.southwest != null && seed.northeast != null &&
            seed.southwest.latitude < seed.northeast.latitude &&
            seed.southwest.longitude < seed.northeast.longitude) {
            // Tirage aléatoire dans la Bounding Box du pays
            val lat = random.nextDouble(seed.southwest.latitude, seed.northeast.latitude)
            val lng = random.nextDouble(seed.southwest.longitude, seed.northeast.longitude)
            LatLng(lat, lng)
        } else {
            // Fallback : Jitter (env +/- 20km)
            val latJitter = (random.nextDouble() - 0.5) * 0.4
            val lngJitter = (random.nextDouble() - 0.5) * 0.4
            LatLng(seed.coordinates.latitude + latJitter, seed.coordinates.longitude + lngJitter)
        }
        
        return seed.copy(coordinates = randomLatLng)
    }
}
