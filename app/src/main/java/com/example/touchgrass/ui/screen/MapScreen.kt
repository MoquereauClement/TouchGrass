package com.example.touchgrass.ui.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.R
import com.example.touchgrass.ui.theme.BackgroundMediumBlack
import com.example.touchgrass.ui.theme.Primary
import com.example.touchgrass.viewmodel.MapViewModel
import com.example.touchgrass.viewmodel.StreetViewViewModel
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * Écran de la carte interactive où l'utilisateur place son marqueur.
 */
@Composable
fun MapScreen(
    streetViewViewModel: StreetViewViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    
    // Observation des données via StateFlow
    val guessedLocation by mapViewModel.guessedLocation.collectAsState()
    val timeLeft by streetViewViewModel.timeLeft.collectAsState()

    // Si le temps est écoulé pendant qu'on est sur la carte, on valide automatiquement
    LaunchedEffect(timeLeft) {
        if (timeLeft <= 0) {
            onNavigateToResult()
        }
    }

    // Chargement de l'icône personnalisée du marqueur
    val myMarkerIcon = remember {
        bitmapDescriptorFromVector(context, R.drawable.my_landmark)
    }

    // Initialisation du MapView (composant Google Maps classique)
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    // Gestion propre du cycle de vie pour éviter les fuites de mémoire
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    // Configuration de la carte une fois qu'elle est prête
    LaunchedEffect(mapView) {
        mapView.getMapAsync { googleMap ->
            // On désactive certains outils par défaut pour épurer l'UI
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Si l'utilisateur a déjà cliqué, on remet le marqueur
            guessedLocation?.let {
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(it)
                        .icon(myMarkerIcon ?: BitmapDescriptorFactory.defaultMarker())
                )
            }

            // Gestion du clic sur la carte pour placer le point
            googleMap.setOnMapClickListener { latLng ->
                // Mise à jour dans le ViewModel
                mapViewModel.updateGuessedLocation(latLng)
                
                // Mise à jour visuelle immédiate
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(myMarkerIcon ?: BitmapDescriptorFactory.defaultMarker())
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Affichage de la carte via AndroidView (pont entre View classique et Compose)
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Top Bar avec le bouton Retour et le Timer (Alignés sur StreetViewScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Bouton Retour stylisé
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(BackgroundMediumBlack.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White)
            }

            // Affichage du Timer au centre (identique au StreetViewScreen pour la cohérence)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(Color(0xFF1A1A1A).copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TEMPS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${timeLeft}s", color = if (timeLeft < 10) Color.Red else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Bouton de validation qui n'apparaît que si l'utilisateur a cliqué
        if (guessedLocation != null) {
            Button(
                onClick = onNavigateToResult,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .height(64.dp)
                    .fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("VALIDER MON CHOIX", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

/**
 * Fonction utilitaire pour transformer un XML Vector Drawable en BitmapDescriptor
 * car Google Maps ne supporte pas directement les Vectors pour les icônes de marqueurs.
 */
fun bitmapDescriptorFromVector(context: android.content.Context, vectorResId: Int): BitmapDescriptor? {
    return try {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) { null }
}
