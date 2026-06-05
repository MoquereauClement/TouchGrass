package com.example.touchgrass.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.R
import com.example.touchgrass.model.GameSettings
import com.example.touchgrass.ui.theme.*
import com.example.touchgrass.viewmodel.Lobby
import com.example.touchgrass.viewmodel.LobbyViewModel
import com.example.touchgrass.viewmodel.ProfileViewModel
import kotlin.random.Random

@Composable
fun CreateGameScreen(
    onBack: () -> Unit,
    onStartGame: (Long, GameSettings) -> Unit,
    lobbyViewModel: LobbyViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val gameSeed = remember { Random.nextLong(100000, 999999) }
    val context = LocalContext.current
    val username by profileViewModel.username.collectAsState()
    val lobby by lobbyViewModel.currentLobby.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var maxPlayers by remember { mutableIntStateOf(8) }
    var rounds by remember { mutableIntStateOf(5) }
    var timePerRound by remember { mutableFloatStateOf(60f) }
    var selectedMode by remember { mutableStateOf("Normal") }

    LaunchedEffect(gameSeed, username, maxPlayers, rounds, timePerRound, selectedMode) {
        if (username.isNotEmpty()) {
            lobbyViewModel.createOrUpdateLobby(
                Lobby(
                    id = gameSeed.toString(),
                    host = username,
                    players = lobby?.players ?: listOf(username),
                    maxPlayers = maxPlayers,
                    rounds = rounds,
                    timeLimit = timePerRound.toInt(),
                    mode = selectedMode
                )
            )
        }
    }

    LaunchedEffect(lobby?.isStarted) {
        if (lobby?.isStarted == true) {
            val settings = GameSettings(
                rounds = lobby?.rounds ?: rounds,
                timeLimit = lobby?.timeLimit ?: timePerRound.toInt(),
                mode = lobby?.mode ?: selectedMode
            )
            onStartGame(gameSeed, settings)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeepBlack)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart).size(48.dp).background(BackgroundMediumBlack.copy(alpha = 0.9f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White) }

                Text(text = "Salon de jeu", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Code d'invitation :", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(20.dp)).background(BackgroundMediumBlack).border(1.dp, ScreenBorder, RoundedCornerShape(20.dp)).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(painterResource(id = R.drawable.qr_code), null, tint = IconsColor, modifier = Modifier.size(28.dp))
                Text(text = gameSeed.toString(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                
                Row {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Game Code", gameSeed.toString())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copié !", Toast.LENGTH_SHORT).show()
                    }) { Icon(painterResource(id = R.drawable.clipboard), null, tint = IconsColor, modifier = Modifier.size(24.dp)) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(BackgroundMediumBlack).border(1.dp, ScreenBorder, RoundedCornerShape(24.dp)).padding(20.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("JOUEURS", color = Secondaire, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("${lobby?.players?.size ?: 1}/$maxPlayers", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val players = lobby?.players ?: listOf(username)
                        items(players) { player ->
                            PlayerRow(player, isHost = player == lobby?.host)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(16.dp)).background(BackgroundMediumBlack).border(1.dp, ScreenBorder, RoundedCornerShape(16.dp)).clickable { showSettings = true }.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Réglages de la partie", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("$rounds rounds • ${timePerRound.toInt()}s • Mode $selectedMode", color = Color.Gray, fontSize = 12.sp)
                }
                Icon(Icons.Default.Settings, null, tint = IconsColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { lobbyViewModel.startLobby(gameSeed.toString()) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                enabled = username.isNotEmpty()
            ) { Text("LANCER LA PARTIE", fontSize = 16.sp, fontWeight = FontWeight.Black) }
        }

        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            SettingsOverlay(
                currentMode = selectedMode,
                onModeChange = { selectedMode = it },
                rounds = rounds,
                onRoundsChange = { rounds = it },
                time = timePerRound.toInt(),
                onTimeChange = { timePerRound = it },
                maxPlayers = maxPlayers,
                onMaxPlayersChange = { maxPlayers = it },
                onClose = { showSettings = false }
            )
        }
    }
}

@Composable
fun SettingsOverlay(
    currentMode: String,
    onModeChange: (String) -> Unit,
    rounds: Int,
    onRoundsChange: (Int) -> Unit,
    time: Int,
    onTimeChange: (Float) -> Unit,
    maxPlayers: Int,
    onMaxPlayersChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(BackgroundDeepBlack).statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            IconButton(onClick = onClose, modifier = Modifier.size(48.dp).align(Alignment.CenterStart).background(BackgroundMediumBlack.copy(alpha = 0.9f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)) 
            { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White) }
            Text("Réglages", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Mode de jeu", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectableButton("Normal", currentMode == "Normal", Modifier.weight(1f)) { onModeChange("Normal") }
            SelectableButton("Fun", currentMode == "Fun", Modifier.weight(1f)) { onModeChange("Fun") }
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp), color = ScreenBorder)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Nombre de rounds", color = Color.Gray, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (rounds > 1) onRoundsChange(rounds - 1) }) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                Text(rounds.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                IconButton(onClick = { if (rounds < 10) onRoundsChange(rounds + 1) }) { Icon(Icons.Default.Add, null, tint = Color.White) }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp), color = ScreenBorder)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Max Joueurs", color = Color.Gray, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (maxPlayers > 2) onMaxPlayersChange(maxPlayers - 1) }) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                Text(maxPlayers.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                IconButton(onClick = { if (maxPlayers < 20) onMaxPlayersChange(maxPlayers + 1) }) { Icon(Icons.Default.Add, null, tint = Color.White) }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp), color = ScreenBorder)

        Text("Temps par round", color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(value = time.toFloat(), onValueChange = onTimeChange, valueRange = 10f..120f, steps = 11, modifier = Modifier.weight(1f))
            Text("${time}s", color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.width(50.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(16.dp)) 
        { Text("ENREGISTRER", fontWeight = FontWeight.Black) }
    }
}

@Composable
fun SelectableButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(56.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Primary else BackgroundMediumBlack).border(1.dp, if (isSelected) Primary else ScreenBorder, RoundedCornerShape(12.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontWeight = FontWeight.Bold) }
}

@Composable
fun PlayerRow(name: String, isHost: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isHost) Primary else Color.Gray), contentAlignment = Alignment.Center) {
            Icon(if (isHost) Icons.Default.Star else Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (isHost) Text("HÔTE", color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}
