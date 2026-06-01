package com.example.touchgrass.model

import kotlinx.serialization.Serializable

@Serializable
data class GameSettings(
    val rounds: Int = 5,
    val timeLimit: Int = 30,
    val mode: String = "Normal",
    val feature: String = "Normal"
)
