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
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    // On récupère les états du ViewModel (StateFlow)
    val targetLocation by viewModel.targetLocation.collectAsState()
    val userCurrentLocation by viewModel.userCurrentLocation.collectAsState()
    val currentRound by viewModel.round.collectAsState()
    val totalScore by viewModel.totalScore.collectAsState()
    val maxRounds by viewModel.maxRounds.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    
    var showExitDialog by remember { mutableStateOf(false) }
    var showRoundOverlay by remember { mutableStateOf(false) }

    // --- LOGIQUE D'ANIMATION (GAMIFICATION) ---

    // Animation de pulsation quand il reste moins de 10 secondes
    val timerScale by animateFloatAsState(
        targetValue = if (timeLeft in 1..9 && timeLeft % 2 == 0) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "timerPulsing"
    )

    // Affichage temporaire du numéro de round au début de chaque manche
    LaunchedEffect(currentRound) {
        showRoundOverlay = true
        delay(2000)
        showRoundOverlay = false
    }

    // Si le temps tombe à zéro, on force le passage à la carte
    LaunchedEffect(timeLeft) {
        if (timeLeft <= 0) onNavigateToMap()
    }

    // Gestion du bouton "Retour" physique pour éviter de quitter par erreur
    BackHandler { showExitDialog = true }

    // On initialise Street View
    val streetView = remember {
        StreetViewPanoramaView(context).apply { onCreate(Bundle()) }
    }

    // Gestion du cycle de vie de la vue Google Maps
    DisposableEffect(streetView) {
        streetView.onStart()
        streetView.onResume()
        
        streetView.getStreetViewPanoramaAsync { panorama ->
            // Configuration des options de navigation
            panorama.isUserNavigationEnabled = true
            panorama.isPanningGesturesEnabled = true
            panorama.isZoomGesturesEnabled = true
            panorama.isStreetNamesEnabled = false

            // Ecouteur pour savoir quand l'utilisateur se déplace
            panorama.setOnStreetViewPanoramaChangeListener { location: StreetViewPanoramaLocation? ->
                if (location?.panoId == null) {
                    // Si Street View ne trouve pas d'image (écran noir), on demande au VM d'en trouver un autre
                    viewModel.handleNoPanorama()
                } else {
                    viewModel.updateCurrentLocation(location.position)
                }
            }
        }

        // Nettoyage quand on quitte l'écran
        onDispose {
            streetView.onPause()
            streetView.onStop()
            streetView.onDestroy()
        }
    }

    // Mise à jour de la position de la caméra quand la cible change
    LaunchedEffect(targetLocation) {
        streetView.getStreetViewPanoramaAsync { panorama ->
            panorama.setPosition(userCurrentLocation ?: targetLocation, 5000)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Intégration de la vue Street View classique dans Compose
        AndroidView(factory = { streetView }, modifier = Modifier.fillMaxSize())

        // Top Bar avec le bouton Retour et le HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(BackgroundMediumBlack.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quitter", tint = Color.White)
            }

            // HUD central harmonisé
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
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

        // Bouton Map (FAB)
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
            Icon(painterResource(id = R.drawable.map), null, modifier = Modifier.size(40.dp), tint = Color(0xFFEDEDED))
        }

        // --- OVERLAY DE TRANSITION ---
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

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = BackgroundMediumBlack,
                shape = RoundedCornerShape(28.dp),
                title = { Text("Quitter ?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Votre progression dans ce round sera perdue.", color = Color.Gray) },
                confirmButton = {
                    Button(
                        onClick = { onQuit() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("QUITTER", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text("CONTINUER", color = Color.White) }
                }
            )
        }
    }
}
