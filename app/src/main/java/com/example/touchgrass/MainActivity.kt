package com.example.touchgrass

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.touchgrass.ui.theme.TouchGrassTheme

class MainActivity : ComponentActivity() {
    private var initialSeed by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)

        setContent {
            TouchGrassTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Navigation(
                        modifier = Modifier.padding(innerPadding),
                        initialSeed = initialSeed,
                        onSeedHandled = { initialSeed = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val data = intent.data
            // Mise à jour du host pour correspondre au lien Firebase Hosting
            if (data != null && (data.host == "touchgrass-86df1.web.app" || data.host == "touchgrass.app")) {
                val path = data.path ?: ""
                if (path.startsWith("/join")) {
                    initialSeed = data.lastPathSegment
                }
            }
        }
    }
}
