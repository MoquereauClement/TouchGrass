package com.example.touchgrass.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.touchgrass.R
import com.example.touchgrass.model.StreetViewLocation
import com.example.touchgrass.model.GameSettings
import com.example.touchgrass.repository.GameRepository
import com.example.touchgrass.service.StreetViewService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.InputStreamReader
import kotlin.math.exp
import kotlin.random.Random

@Serializable
data class GeoProperties(
    val label_x: Double? = null, 
    val label_y: Double? = null,
    val name: String? = null,
    val name_fr: String? = null
)

@Serializable
data class GeoGeometry(
    val type: String,
    val coordinates: JsonElement
)

@Serializable
data class GeoFeature(
    val properties: GeoProperties,
    val geometry: GeoGeometry? = null
)

@Serializable
data class GeoRoot(val features: List<GeoFeature>)

class StreetViewViewModel(application: Application) : AndroidViewModel(application) {
    private val streetViewService = StreetViewService()
    private val gameRepository = GameRepository()
    private val json = Json { ignoreUnknownKeys = true }

    private val _targetLocation = MutableStateFlow(LatLng(48.8584, 2.2945))
    val targetLocation: StateFlow<LatLng> = _targetLocation.asStateFlow()

    private val _currentCountry = MutableStateFlow("Inconnu")
    val currentCountry: StateFlow<String> = _currentCountry.asStateFlow()

    private val _userCurrentLocation = MutableStateFlow<LatLng?>(null)
    val userCurrentLocation: StateFlow<LatLng?> = _userCurrentLocation.asStateFlow()

    private val _round = MutableStateFlow(1)
    val round: StateFlow<Int> = _round.asStateFlow()

    private val _totalScore = MutableStateFlow(0)
    val totalScore: StateFlow<Int> = _totalScore.asStateFlow()

    private val _gameSeed = MutableStateFlow<Long?>(null)
    val gameSeed: StateFlow<Long?> = _gameSeed.asStateFlow()

    private val _maxRounds = MutableStateFlow(5)
    val maxRounds: StateFlow<Int> = _maxRounds.asStateFlow()
    
    private val _timeLimit = MutableStateFlow(30)
    val timeLimit: StateFlow<Int> = _timeLimit.asStateFlow()
    
    private var currentRetryCount = 0

    init {
        loadSeedsFromGeoJson()
    }

    private fun loadSeedsFromGeoJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().resources.openRawResource(R.raw.custom_geo)
                val jsonString = InputStreamReader(inputStream).readText()
                val root = json.decodeFromString<GeoRoot>(jsonString)
                
                val seeds = root.features.mapNotNull { feature ->
                    val lat = feature.properties.label_y
                    val lng = feature.properties.label_x
                    val name = feature.properties.name_fr ?: feature.properties.name ?: "Inconnu"
                    
                    if (lat != null && lng != null) {
                        val bbox = calculateBBox(feature.geometry)
                        StreetViewLocation(
                            coordinates = LatLng(lat, lng),
                            countryName = name,
                            southwest = bbox?.first,
                            northeast = bbox?.second
                        )
                    } else null
                }

                withContext(Dispatchers.Main) {
                    if (seeds.isNotEmpty()) {
                        streetViewService.setSeeds(seeds)
                    }
                    if (_gameSeed.value == null) {
                        startNewGame()
                    }
                }
            } catch (e: Exception) {
                Log.e("StreetViewViewModel", "Erreur GeoJSON: ${e.message}", e)
                if (_gameSeed.value == null) startNewGame()
            }
        }
    }

    private fun calculateBBox(geometry: GeoGeometry?): Pair<LatLng, LatLng>? {
        if (geometry == null) return null
        var minLat = Double.MAX_VALUE; var maxLat = -Double.MAX_VALUE
        var minLng = Double.MAX_VALUE; var maxLng = -Double.MAX_VALUE
        var found = false

        fun processCoordinates(array: JsonArray) {
            if (array.isEmpty()) return
            if (array[0] is JsonPrimitive) {
                val lng = array[0].jsonPrimitive.double
                val lat = array[1].jsonPrimitive.double
                minLat = minOf(minLat, lat); maxLat = maxOf(maxLat, lat)
                minLng = minOf(minLng, lng); maxLng = maxOf(maxLng, lng)
                found = true
            } else {
                for (element in array) { if (element is JsonArray) processCoordinates(element) }
            }
        }

        try { processCoordinates(geometry.coordinates.jsonArray) } catch (e: Exception) { return null }
        return if (found) Pair(LatLng(minLat, minLng), LatLng(maxLat, maxLng)) else null
    }

    fun startNewGame(seed: Long? = null, settings: GameSettings? = null) {
        val finalSeed = seed ?: Random.nextLong(100000, 999999)
        _gameSeed.value = finalSeed
        _round.value = 1
        _totalScore.value = 0
        currentRetryCount = 0
        
        val finalSettings = settings ?: GameSettings()
        _maxRounds.value = finalSettings.rounds
        _timeLimit.value = finalSettings.timeLimit
        
        // Si c'est une nouvelle partie solo sans settings, on enregistre les par défaut pour le partage
        if (settings == null) {
            viewModelScope.launch {
                gameRepository.saveGameSettings(finalSeed, finalSettings)
            }
        }
        
        generateRandomLocation()
    }

    fun saveAndStartGame(seed: Long, settings: GameSettings) {
        viewModelScope.launch {
            gameRepository.saveGameSettings(seed, settings)
            startNewGame(seed, settings)
        }
    }

    fun loadGameAndStart(seed: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val settings = gameRepository.getGameSettings(seed)
            if (settings != null) {
                // On utilise les réglages récupérés pour lancer la partie identique
                startNewGame(seed, settings)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun generateRandomLocation() {
        val seed = _gameSeed.value ?: return
        val location = streetViewService.getDeterministicLocation(seed, _round.value, currentRetryCount)
        _targetLocation.value = location.coordinates
        _currentCountry.value = location.countryName
        _userCurrentLocation.value = null 
    }
    
    fun handleNoPanorama() {
        currentRetryCount++
        generateRandomLocation()
    }

    fun calculatePoints(distanceInKm: Double): Int {
        if (distanceInKm >= 7000.0) return 0
        val points = 5000.0 * exp(-0.0008 * distanceInKm)
        return points.toInt().coerceIn(0, 5000)
    }

    fun completeRound(roundPoints: Int) {
        _totalScore.value += roundPoints
        if (_round.value < _maxRounds.value) {
            _round.value += 1
            currentRetryCount = 0
            generateRandomLocation()
        }
    }

    fun updateCurrentLocation(position: LatLng) {
        _userCurrentLocation.value = position
    }
}
