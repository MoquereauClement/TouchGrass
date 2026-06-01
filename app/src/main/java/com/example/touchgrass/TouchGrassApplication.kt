package com.example.touchgrass

import android.app.Application
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class TouchGrassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialisation de Firebase
        FirebaseApp.initializeApp(this)

        // Configuration de Firestore pour être plus robuste
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                    .setSizeBytes(100 * 1024 * 1024) // 100 MB de cache
                    .build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            Log.e("TouchGrassApp", "Erreur configuration Firestore", e)
        }

        // Initialisation de Maps et mise à jour du fournisseur de sécurité
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) { renderer ->
            Log.d("MapsInit", "Renderer utilisé : $renderer")
        }

        // Essayer de mettre à jour le Security Provider (aide pour SSL/DNS)
        ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstalled() {
                Log.d("SecurityProvider", "Provider installé avec succès")
            }

            override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: android.content.Intent?) {
                Log.e("SecurityProvider", "Échec de l'installation du provider: $errorCode")
            }
        })
    }
}
