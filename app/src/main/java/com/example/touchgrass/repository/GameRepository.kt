package com.example.touchgrass.repository

import android.util.Log
import com.example.touchgrass.model.GameSettings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val gamesCollection = db.collection("games")

    suspend fun saveGameSettings(seed: Long, settings: GameSettings) {
        try {
            Log.d("GameRepository", "Saving settings for seed $seed: $settings")
            gamesCollection.document(seed.toString()).set(settings).await()
            Log.d("GameRepository", "Settings saved successfully")
        } catch (e: Exception) {
            Log.e("GameRepository", "Error saving game settings", e)
        }
    }

    suspend fun getGameSettings(seed: Long): GameSettings? {
        return try {
            Log.d("GameRepository", "Fetching settings for seed $seed")
            val snapshot = gamesCollection.document(seed.toString()).get().await()
            if (snapshot.exists()) {
                val settings = snapshot.toObject(GameSettings::class.java)
                Log.d("GameRepository", "Settings found: $settings")
                settings
            } else {
                Log.d("GameRepository", "No settings found for seed $seed")
                null
            }
        } catch (e: Exception) {
            Log.e("GameRepository", "Error fetching game settings", e)
            null
        }
    }
}
