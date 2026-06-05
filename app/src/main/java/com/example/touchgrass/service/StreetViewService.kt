package com.example.touchgrass.service

import com.example.touchgrass.model.StreetViewLocation
import com.google.android.gms.maps.model.LatLng
import kotlin.random.Random

/**
 * Service pour gérer la logique de génération des positions
 */
class StreetViewService {
    private var countrySeeds: List<StreetViewLocation> = emptyList()

    fun setSeeds(seeds: List<StreetViewLocation>) {
        this.countrySeeds = seeds
    }

    /**
     * Génère une position aléatoire mais fixe pour un même code (seed).
     * Cela permet d'avoir les mêmes lieux en mode multijoueur
     */
    fun getDeterministicLocation(gameSeed: Long, round: Int, retryCount: Int): StreetViewLocation {
        val uniqueSeed = gameSeed + (round * 1000) + retryCount
        val random = Random(uniqueSeed)

        val country = if (countrySeeds.isNotEmpty()) {
            val index = random.nextInt(countrySeeds.size)
            countrySeeds[index]
        } else {
            StreetViewLocation(LatLng(48.8584, 2.2945), "France")
        }

        // Calcul d'une position aléatoire dans la zone du pays (Bounding Box)
        val finalLatLng = if (country.southwest != null && country.northeast != null &&
            country.southwest.latitude < country.northeast.latitude) {
            
            val randomLat = random.nextDouble(country.southwest.latitude, country.northeast.latitude)
            val randomLng = random.nextDouble(country.southwest.longitude, country.northeast.longitude)
            LatLng(randomLat, randomLng)
        } else {
            // Décalage aléatoire autour du point central si on n'a pas de zone précise
            val offsetLat = (random.nextDouble() - 0.5) * 0.4
            val offsetLng = (random.nextDouble() - 0.5) * 0.4
            LatLng(country.coordinates.latitude + offsetLat, country.coordinates.longitude + offsetLng)
        }
        
        return country.copy(coordinates = finalLatLng)
    }
}
