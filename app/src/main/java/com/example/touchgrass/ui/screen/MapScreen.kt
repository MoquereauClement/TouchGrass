package com.example.touchgrass.ui.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.R
import com.example.touchgrass.viewmodel.MapViewModel
import com.example.touchgrass.viewmodel.StreetViewViewModel
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen(
    streetViewViewModel: StreetViewViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    val guessedLocation by mapViewModel.guessedLocation.collectAsState()

    val myMarkerIcon = remember {
        bitmapDescriptorFromVector(context, R.drawable.my_landmark)
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    // Gestion du cycle de vie de la Map
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.uiSettings.isZoomControlsEnabled = true

            guessedLocation?.let {
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(it)
                        .icon(myMarkerIcon ?: BitmapDescriptorFactory.defaultMarker())
                        .title("Votre choix")
                )
            }

            googleMap.setOnMapClickListener { latLng ->
                mapViewModel.updateGuessedLocation(latLng)

                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(myMarkerIcon ?: BitmapDescriptorFactory.defaultMarker())
                        .title("Votre choix")
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            update = { },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp, 50.dp)
        ) {
            Text("Retour")
        }

        if (guessedLocation != null) {
            Button(
                onClick = onNavigateToResult,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                Text("Valider mon choix")
            }
        }

    }
}


fun bitmapDescriptorFromVector(
    context: android.content.Context,
    vectorResId: Int
): BitmapDescriptor? {
    return try {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        null
    }
}
