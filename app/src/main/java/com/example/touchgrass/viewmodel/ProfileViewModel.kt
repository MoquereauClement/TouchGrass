package com.example.touchgrass.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val db = FirebaseFirestore.getInstance()
    
    private val _username = MutableStateFlow(prefs.getString("username", "") ?: "")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow(prefs.getString("email", "") ?: "")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun setUsername(name: String) {
        val normalizedName = name.trim()
        val currentEmail = _email.value.lowercase().trim()
        if (currentEmail.isNotEmpty()) {
            db.collection("users").document(currentEmail).update("username", normalizedName)
                .addOnFailureListener { e -> Log.e("ProfileViewModel", "Update failed", e) }
        }
        prefs.edit().putString("username", normalizedName).apply()
        _username.value = normalizedName
    }

    suspend fun checkUserRedundancy(email: String, username: String): String? {
        val normalizedEmail = email.lowercase().trim()
        val normalizedUsername = username.trim()
        
        return try {
            val emailDoc = db.collection("users").document(normalizedEmail).get().await()
            if (emailDoc.exists()) {
                val registeredName = emailDoc.getString("username") ?: ""
                if (!registeredName.equals(normalizedUsername, ignoreCase = true)) {
                    return "Cet e-mail est lié au pseudo : $registeredName"
                }
            } else {
                val nameQuery = db.collection("users")
                    .whereEqualTo("username", normalizedUsername)
                    .get()
                    .await()
                if (!nameQuery.isEmpty) {
                    return "Ce pseudo est déjà pris par un autre aventurier."
                }
            }
            null
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Firestore check error: ${e.message}", e)
            "Erreur de connexion au serveur. Vérifiez que Firestore est activé."
        }
    }

    fun login(email: String, name: String) {
        val normalizedEmail = email.lowercase().trim()
        val normalizedName = name.trim()
        
        db.collection("users").document(normalizedEmail).set(mapOf("username" to normalizedName))
            .addOnFailureListener { e -> Log.e("ProfileViewModel", "Login Firestore save failed", e) }

        prefs.edit()
            .putString("email", normalizedEmail)
            .putString("username", normalizedName)
            .putBoolean("is_logged_in", true)
            .apply()
        _email.value = normalizedEmail
        _username.value = normalizedName
        _isLoggedIn.value = true
    }

    fun logout() {
        prefs.edit()
            .remove("email")
            .remove("username")
            .putBoolean("is_logged_in", false)
            .apply()
        _email.value = ""
        _username.value = ""
        _isLoggedIn.value = false
    }
}
