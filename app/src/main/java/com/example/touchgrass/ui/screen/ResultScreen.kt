package com.example.touchgrass.ui.screen

import android.os.Bundle
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.R
import com.example.touchgrass.viewmodel.MapViewModel
import com.example.touchgrass.viewmodel.StreetViewViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(
    streetViewViewModel: StreetViewViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onNextRound: () -> Unit,
    onGameOver: () -> Unit
) {
    val context = LocalContext.current

    val targetLoc = remember { streetViewViewModel.targetLocation.value }
    val guessedLoc = remember { mapViewModel.guessedLocation.value }
    val currentRound = remember { streetViewViewModel.round.value }
    val maxRounds by streetViewViewModel.maxRounds.collectAsState()
    val totalScoreAtStart = remember { streetViewViewModel.totalScore.value }
    val gameSeed by streetViewViewModel.gameSeed.collectAsState()
    
    val distance = remember(targetLoc, guessedLoc) {
        guessedLoc?.let { mapViewModel.calculateDistance(targetLoc, it) }
    }

    val roundPoints = remember(distance) {
        distance?.let { streetViewViewModel.calculatePoints(it) } ?: 0
    }

    var startScoreAnim by remember { mutableStateOf(false) }
    var showGrade by remember { mutableStateOf(false) }
    
    val animatedPoints by animateIntAsState(
        targetValue = if (startScoreAnim) roundPoints else 0,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "scoreAnim"
    )

    val gradeScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        startScoreAnim = true
        delay(1300)
        showGrade = true
        gradeScale.animateTo(1.2f, animationSpec = tween(500, easing = EaseOutBack))
        gradeScale.animateTo(1.0f, animationSpec = tween(200))
    }

    val (gradeText, gradeColor) = remember(showGrade, roundPoints) {
        if (!showGrade || distance == null) "" to Color.Transparent
        else when {
            roundPoints >= 4950 -> "PARFAIT !" to Color(0xFF4CAF50)
            roundPoints >= 4500 -> "INCROYABLE" to Color(0xFF8BC34A)
            roundPoints >= 3000 -> "BIEN JOUÉ" to Color(0xFFFFEB3B)
            roundPoints >= 1000 -> "PAS MAL" to Color(0xFFFF9800)
            else -> "DOMMAGE..." to Color(0xFFF44336)
        }
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            update = { view ->
                view.getMapAsync { googleMap ->
                    googleMap.clear()
                    val boundsBuilder = LatLngBounds.Builder()
                    boundsBuilder.include(targetLoc)
                    googleMap.addMarker(MarkerOptions().position(targetLoc).icon(bitmapDescriptorFromVector(context, R.drawable.target_landmark)))
                    guessedLoc?.let { guess ->
                        boundsBuilder.include(guess)
                        googleMap.addMarker(MarkerOptions().position(guess).icon(bitmapDescriptorFromVector(context, R.drawable.my_landmark)))
                        googleMap.addPolyline(PolylineOptions().add(targetLoc, guess).color(android.graphics.Color.DKGRAY).width(8f).pattern(listOf(Dash(20f), Gap(10f))))
                    }
                    try { googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 250)) }
                    catch (e: Exception) { googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 10f)) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1A1A1A).copy(alpha = 0.95f), RoundedCornerShape(32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (gradeText.isNotEmpty()) {
                Text(gradeText, color = gradeColor, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.scale(gradeScale.value))
            } else {
                Spacer(modifier = Modifier.height(38.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$animatedPoints", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("POINTS", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))
            distance?.let {
                Text("${String.format("%.1f", it)} km de distance", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
            Spacer(modifier = Modifier.height(16.dp))
            Text("SCORE TOTAL : ${totalScoreAtStart + animatedPoints}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // Bouton Continuer + Affichage Seed
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            gameSeed?.let {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("CODE : $it", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    streetViewViewModel.completeRound(roundPoints)
                    mapViewModel.reset()
                    if (currentRound < maxRounds) onNextRound() else onGameOver()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52489C)),
                modifier = Modifier.height(60.dp).fillMaxWidth(0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (currentRound < maxRounds) "CONTINUER" else "VOIR LE BILAN", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
