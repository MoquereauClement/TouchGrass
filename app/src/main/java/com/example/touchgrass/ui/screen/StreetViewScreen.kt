package com.example.touchgrass.ui.screen

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.R
import com.example.touchgrass.ui.theme.*
import com.example.touchgrass.viewmodel.StreetViewViewModel
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.StreetViewPanoramaLocation
import kotlinx.coroutines.delay

@Composable
fun StreetViewScreen(
    modifier: Modifier = Modifier,
    viewModel: StreetViewViewModel = viewModel(),
    onNavigateToMap: () -> Unit,
    onQuit: () -> Unit
) {
    val context = LocalContext.current
    val targetLocation by viewModel.targetLocation.collectAsState()
    val userCurrentLocation by viewModel.userCurrentLocation.collectAsState()
    val currentRound by viewModel.round.collectAsState()
    val totalScore by viewModel.totalScore.collectAsState()
    val maxRounds by viewModel.maxRounds.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    
    var showExitDialog by remember { mutableStateOf(false) }
    var showRoundOverlay by remember { mutableStateOf(false) }

    // Animation de pulsation pour le timer si < 10s
    val timerScale by animateFloatAsState(
        targetValue = if (timeLeft in 1..9 && timeLeft % 2 == 0) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "timerPulsing"
    )

    // Gestion de l'overlay de début de round
    LaunchedEffect(currentRound) {
        showRoundOverlay = true
        delay(2000)
        showRoundOverlay = false
    }

    // Navigation automatique si le temps est écoulé
    LaunchedEffect(timeLeft) {
        if (timeLeft <= 0) {
            onNavigateToMap()
        }
    }

    // Gère le bouton retour physique du téléphone
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    val streetView = remember {
        StreetViewPanoramaView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(streetView) {
        streetView.onStart()
        streetView.onResume()
        
        streetView.getStreetViewPanoramaAsync { panorama ->
            panorama.isUserNavigationEnabled = true
            panorama.isPanningGesturesEnabled = true
            panorama.isZoomGesturesEnabled = true
            panorama.isStreetNamesEnabled = false

            panorama.setOnStreetViewPanoramaChangeListener { location: StreetViewPanoramaLocation? ->
                if (location == null || location.panoId == null) {
                    viewModel.handleNoPanorama()
                } else {
                    viewModel.updateCurrentLocation(location.position)
                }
            }
        }

        onDispose { 
            streetView.onPause()
            streetView.onStop()
            streetView.onDestroy()
        }
    }

    LaunchedEffect(targetLocation) {
        streetView.getStreetViewPanoramaAsync { panorama ->
            panorama.setPosition(userCurrentLocation ?: targetLocation, 5000)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { streetView }, modifier = Modifier.fillMaxSize())

        // Top Bar Harmonisée
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Bouton Retour
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(BackgroundMediumBlack.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quitter la partie",
                    tint = Color.White
                )
            }

            // HUD central (Round, Temps, Score)
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xFF1A1A1A).copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ROUND", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$currentRound/$maxRounds", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.width(16.dp))
                
                // Timer Animé (Gamification : Pulsation)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(timerScale)
                ) {
                    Text("TEMPS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${timeLeft}s", color = if (timeLeft < 10) Color.Red else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SCORE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$totalScore", color = Color(0xFF4CAF50), fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToMap,
            containerColor = Color(0xFF52489C),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 50.dp, end = 30.dp)
                .size(80.dp)
                .border(2.dp, Color(0xFFEDEDED), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.map),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFEDEDED)
            )
        }

        // --- Overlay de début de Round (Gamification : Animation d'entrée) ---
        AnimatedVisibility(
            visible = showRoundOverlay,
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .border(2.dp, Primary, RoundedCornerShape(16.dp))
                    .padding(horizontal = 40.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ROUND $currentRound",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            }
        }

        // Pop-up de confirmation
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = BackgroundMediumBlack,
                shape = RoundedCornerShape(28.dp),
                title = {
                    Text(
                        "Quitter la partie ?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Êtes-vous sûr de vouloir abandonner ? Votre progression dans ce round sera perdue.",
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onQuit()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("QUITTER", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("CONTINUER", color = Color.White)
                    }
                }
            )
        }
    }
}
