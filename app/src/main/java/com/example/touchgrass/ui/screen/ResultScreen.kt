package com.example.touchgrass.ui.screen

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Écran qui affiche le résultat du round : distance, score et animation sur la carte.
 */
@Composable
fun ResultScreen(
    streetViewViewModel: StreetViewViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onNextRound: () -> Unit,
    onGameOver: () -> Unit
) {
    val context = LocalContext.current

    // On récupère les positions (cible et choix utilisateur) depuis les ViewModels
    val targetLoc = remember { streetViewViewModel.targetLocation.value }
    val guessedLoc = remember { mapViewModel.guessedLocation.value }
    val currentRound = remember { streetViewViewModel.round.value }
    val maxRounds by streetViewViewModel.maxRounds.collectAsState()
    val totalScoreAtStart = remember { streetViewViewModel.totalScore.value }
    val gameSeed by streetViewViewModel.gameSeed.collectAsState()
    
    // Calcul de la distance réelle entre les deux points
    val distance = remember(targetLoc, guessedLoc) {
        guessedLoc?.let { mapViewModel.calculateDistance(targetLoc, it) }
    }

    // Calcul du score via la formule du ViewModel
    val roundPoints = remember(distance) {
        distance?.let { streetViewViewModel.calculatePoints(it) } ?: 0
    }

    // --- ÉTATS POUR LES ANIMATIONS ---
    var startScoreAnim by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }
    
    // Animation du compteur de points (monte de 0 à roundPoints)
    val animatedPoints by animateIntAsState(
        targetValue = if (startScoreAnim) roundPoints else 0,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "scoreAnim"
    )

    // Animation de la barre horizontale
    val progressAnim by animateFloatAsState(
        targetValue = if (startScoreAnim) roundPoints / 5000f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
    )

    // Couleur dynamique (Rouge -> Vert) selon le score
    val barColor by animateColorAsState(
        targetValue = when {
            roundPoints >= 4500 -> Color(0xFF4CAF50) // Vert
            roundPoints >= 3000 -> Color(0xFFFFEB3B) // Jaune
            roundPoints >= 1000 -> Color(0xFFFF9800) // Orange
            else -> Color(0xFFF44336) // Rouge
        }
    )

    // Echelle pour faire apparaître le texte de mention (INCROYABLE, etc)
    val gradeScale = remember { Animatable(0f) }

    // --- LANCEMENT DES ANIMATIONS ---
    LaunchedEffect(Unit) {
        delay(300)
        startScoreAnim = true
        
        // On attend que le composant Google Map soit prêt avant d'animer la caméra
        while (googleMapRef == null) delay(100)
        val map = googleMapRef!!

        if (guessedLoc != null) {
            // 1. Focus sur le clic du joueur
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(guessedLoc, 10f))
            delay(1000)
            
            // 2. Création de la vue d'ensemble (Zoom Out)
            val baseBounds = LatLngBounds.Builder()
                .include(targetLoc)
                .include(guessedLoc)
                .build()
            
            val latSpan = baseBounds.northeast.latitude - baseBounds.southwest.latitude
            
            // ASTUCE : On ajoute un point invisible au dessus du trajet pour décaler 
            // la vue vers le bas, sinon les marqueurs sont cachés par le cadre du score.
            val virtualNorth = LatLng(baseBounds.northeast.latitude + latSpan * 1.0, baseBounds.center.longitude)
            val finalBounds = LatLngBounds.Builder()
                .include(targetLoc).include(guessedLoc).include(virtualNorth).build()
            
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(finalBounds, 200), 2500, null)
        }

        delay(1500)
        // Si le score est top, on fait la fête !
        if (roundPoints >= 4500) {
            showConfetti = true
            gradeScale.animateTo(1.2f, animationSpec = tween(500, easing = EaseOutBack))
            gradeScale.animateTo(1.0f, animationSpec = tween(200))
        }
    }

    // Libellé de félicitation
    val gradeText = remember(roundPoints) {
        when {
            roundPoints >= 4950 -> "PARFAIT !"
            roundPoints >= 4500 -> "INCROYABLE"
            roundPoints >= 3000 -> "BIEN JOUÉ"
            roundPoints >= 1000 -> "PAS MAL"
            else -> "DOMMAGE..."
        }
    }

    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Affichage de la carte et des tracés
        AndroidView(
            factory = { mapView },
            update = { view ->
                view.getMapAsync { googleMap ->
                    if (googleMapRef == null) {
                        googleMapRef = googleMap
                        googleMap.clear()
                        // Ajout du drapeau cible
                        googleMap.addMarker(MarkerOptions().position(targetLoc).icon(bitmapDescriptorFromVector(context, R.drawable.target_landmark)))
                        guessedLoc?.let { guess ->
                            // Ajout du marqueur joueur
                            googleMap.addMarker(MarkerOptions().position(guess).icon(bitmapDescriptorFromVector(context, R.drawable.my_landmark)))
                            // Tracé de la ligne en pointillés
                            googleMap.addPolyline(PolylineOptions()
                                .add(targetLoc, guess)
                                .color(android.graphics.Color.DKGRAY)
                                .width(8f)
                                .pattern(listOf(Dash(20f), Gap(10f))))
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Affichage des confettis (uniquement si score élevé)
        if (showConfetti) { ConfettiEffect() }

        // --- INTERFACE DU SCORE (Overlay) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f)
                .background(Color(0xFF1A1A1A).copy(alpha = 0.95f), RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gradeText,
                color = barColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(if (showConfetti) gradeScale.value else 1f)
            )
            
            Text(text = "$animatedPoints", fontSize = 56.sp, fontWeight = FontWeight.Black, color = Color.White)
            
            // Barre de progression visuelle
            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))) {
                Box(modifier = Modifier.fillMaxWidth(progressAnim).fillMaxHeight().clip(CircleShape).background(barColor))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("POINTS SUR 5000", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))
            distance?.let {
                Text("${String.format("%.1f", it)} km de distance", color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
            Spacer(modifier = Modifier.height(16.dp))
            Text("SCORE TOTAL : ${totalScoreAtStart + animatedPoints}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Section basse : Code de partie et bouton de progression
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            gameSeed?.let {
                Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("CODE : $it", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                modifier = Modifier.height(64.dp).fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (currentRound < maxRounds) "CONTINUER" else "VOIR LE BILAN", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}

/**
 * Système de particules simple pour simuler des confettis.
 */
@Composable
fun ConfettiEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val particles = remember { List(50) { ConfettiParticle() } }

    particles.forEach { particle ->
        val yOffset by infiniteTransition.animateFloat(
            initialValue = -50f,
            targetValue = 2000f,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(particle.delay)
            ),
            label = "y"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(particle.xPos * size.width, yOffset)
            )
        }
    }
}

/**
 * Modèle de données pour un seul confetti.
 */
data class ConfettiParticle(
    val xPos: Float = Random.nextFloat(),
    val color: Color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f),
    val size: Float = Random.nextInt(10, 25).toFloat(),
    val duration: Int = Random.nextInt(2000, 4000),
    val delay: Int = Random.nextInt(0, 3000)
)
