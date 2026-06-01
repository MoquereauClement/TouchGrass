package com.example.touchgrass.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Lobby(
    val id: String = "",
    val host: String = "",
    val players: List<String> = emptyList(),
    val maxPlayers: Int = 8,
    val rounds: Int = 5,
    val timeLimit: Int = 30,
    val mode: String = "Normal",
    val feature: String = "Normal",
    val isStarted: Boolean = false
)

class LobbyViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _currentLobby = MutableStateFlow<Lobby?>(null)
    val currentLobby: StateFlow<Lobby?> = _currentLobby.asStateFlow()

    fun createOrUpdateLobby(lobby: Lobby) {
        db.collection("lobbies").document(lobby.id).set(lobby)
            .addOnFailureListener { Log.e("LobbyViewModel", "Error saving lobby", it) }
        listenToLobby(lobby.id)
    }

    fun joinLobby(seed: String, username: String, onSuccess: (Lobby) -> Unit, onFailure: () -> Unit) {
        db.collection("lobbies").document(seed).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val lobby = doc.toObject<Lobby>()
                if (lobby != null) {
                    if (!lobby.players.contains(username)) {
                        val newPlayers = lobby.players + username
                        db.collection("lobbies").document(seed).update("players", newPlayers)
                    }
                    listenToLobby(seed)
                    onSuccess(lobby)
                } else onFailure()
            } else onFailure()
        }.addOnFailureListener { onFailure() }
    }

    fun listenToLobby(seed: String) {
        db.collection("lobbies").document(seed).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                _currentLobby.value = snapshot.toObject<Lobby>()
            }
        }
    }

    fun startLobby(seed: String) {
        db.collection("lobbies").document(seed).update("isStarted", true)
    }
}
