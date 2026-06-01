package com.example.touchgrass.repository

import android.util.Log
import com.example.touchgrass.model.ScoreEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class LeaderboardRepository {
    private val db = FirebaseFirestore.getInstance()
    private val scoresCollection = db.collection("scores")

    suspend fun submitScore(entry: ScoreEntry) {
        try {
            scoresCollection.add(entry).await()
        } catch (e: Exception) {
            Log.e("LeaderboardRepo", "Erreur lors de l'envoi du score", e)
        }
    }

    suspend fun getLeaderboardForSeed(seed: Long): List<ScoreEntry> {
        return try {
            val querySnapshot = scoresCollection
                .whereEqualTo("seed", seed)
                .get()
                .await()
            
            querySnapshot.toObjects(ScoreEntry::class.java)
                .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.timestamp })
                .take(10)
        } catch (e: Exception) {
            Log.e("LeaderboardRepo", "Erreur récupération seed", e)
            emptyList()
        }
    }

    suspend fun getGlobalLeaderboard(): List<ScoreEntry> {
        return try {
            // On récupère les meilleurs scores globaux
            scoresCollection
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
                .toObjects(ScoreEntry::class.java)
                .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.timestamp })
                .take(20)
        } catch (e: Exception) {
            Log.e("LeaderboardRepo", "Erreur récupération global", e)
            emptyList()
        }
    }
}
