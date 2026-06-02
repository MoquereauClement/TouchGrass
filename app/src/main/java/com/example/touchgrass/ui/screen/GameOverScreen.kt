package com.example.touchgrass.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.viewmodel.StreetViewViewModel
import com.example.touchgrass.viewmodel.LeaderboardViewModel
import com.example.touchgrass.viewmodel.ProfileViewModel
import com.example.touchgrass.ui.theme.*

@Composable
fun GameOverScreen(
    streetViewModel: StreetViewViewModel = viewModel(),
    leaderboardViewModel: LeaderboardViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    onBackToMenu: () -> Unit
) {
    val totalScore by streetViewModel.totalScore.collectAsState()
    val gameSeed by streetViewModel.gameSeed.collectAsState()
    val username by profileViewModel.username.collectAsState()
    val leaderboard by leaderboardViewModel.leaderboard.collectAsState()
    val isSubmitting by leaderboardViewModel.isSubmitting.collectAsState()
    val context = LocalContext.current

    var scoreSubmitted by remember { mutableStateOf(false) }
    var showGlobalRank by remember { mutableStateOf(false) }
    
    val displayName = username.ifEmpty { "Aventurier" }

    LaunchedEffect(gameSeed, showGlobalRank) {
        if (showGlobalRank) {
            leaderboardViewModel.fetchGlobalLeaderboard()
        } else {
            gameSeed?.let { leaderboardViewModel.fetchLeaderboard(it) }
        }
    }

    LaunchedEffect(displayName, totalScore, gameSeed) {
        if (gameSeed != null && !scoreSubmitted) {
            leaderboardViewModel.submitScore(displayName, totalScore.toLong(), gameSeed!!)
            scoreSubmitted = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Harmonisée
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            IconButton(
                onClick = onBackToMenu,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .background(BackgroundMediumBlack.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Menu", tint = Color.White)
            }

            Text(
                text = "Bilan",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Score final
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SCORE FINAL", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("$totalScore", color = Color(0xFF4CAF50), fontSize = 80.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Code de partie
        gameSeed?.let { seed ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BackgroundMediumBlack),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CODE DE LA PARTIE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$seed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Game Seed", seed.toString()))
                        Toast.makeText(context, "Code copié !", Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Default.ContentCopy, null, tint = Color.White) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sélecteur de Classement
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BackgroundMediumBlack)
                .padding(4.dp)
        ) {
            RankTab(text = "CETTE PARTIE", isSelected = !showGlobalRank, modifier = Modifier.weight(1f)) { showGlobalRank = false }
            RankTab(text = "GLOBAL", isSelected = showGlobalRank, modifier = Modifier.weight(1f)) { showGlobalRank = true }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Liste du classement
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundMediumBlack)
                .border(1.dp, ScreenBorder, RoundedCornerShape(20.dp))
        ) {
            if (isSubmitting && leaderboard.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
            } else if (leaderboard.isEmpty()) {
                Text("Aucun score", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(leaderboard) { index, entry ->
                        LeaderboardRow(index + 1, entry.username, entry.score, entry.username == displayName)
                        if (index < leaderboard.size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackToMenu,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("RETOUR AU MENU", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun RankTab(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun LeaderboardRow(rank: Int, name: String, score: Long, isMe: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "#$rank", color = if (rank <= 3) Primary else Color.Gray, fontWeight = FontWeight.Black, modifier = Modifier.width(40.dp))
        Text(text = if (isMe) "$name (Moi)" else name, color = if (isMe) Primary else Color.White, fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(text = "$score", color = Color.White, fontWeight = FontWeight.Black)
    }
}
