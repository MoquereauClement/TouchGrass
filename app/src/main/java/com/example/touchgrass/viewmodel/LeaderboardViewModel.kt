package com.example.touchgrass.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touchgrass.model.ScoreEntry
import com.example.touchgrass.repository.LeaderboardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val repository = LeaderboardRepository()

    private val _leaderboard = MutableStateFlow<List<ScoreEntry>>(emptyList())
    val leaderboard: StateFlow<List<ScoreEntry>> = _leaderboard

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    fun submitScore(username: String, score: Long, seed: Long) {
        viewModelScope.launch {
            _isSubmitting.value = true
            Log.d("LeaderboardVM", "Envoi du score: $score pour $username (seed: $seed)")
            
            repository.submitScore(ScoreEntry(username, score, seed))
            
            // On attend un peu plus pour laisser Firestore indexer le nouveau document
            delay(1500)
            
            // On rafraîchit les scores pour cette seed
            val scores = repository.getLeaderboardForSeed(seed)
            _leaderboard.value = scores
            _isSubmitting.value = false
        }
    }

    fun fetchLeaderboard(seed: Long) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val scores = repository.getLeaderboardForSeed(seed)
            _leaderboard.value = scores
            _isSubmitting.value = false
        }
    }

    fun fetchGlobalLeaderboard() {
        viewModelScope.launch {
            _isSubmitting.value = true
            val scores = repository.getGlobalLeaderboard()
            _leaderboard.value = scores
            _isSubmitting.value = false
        }
    }
}
