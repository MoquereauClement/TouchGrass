package com.example.touchgrass.model

import kotlinx.serialization.Serializable

@Serializable
data class ScoreEntry(
    val username: String = "",
    val score: Long = 0L,
    val seed: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
