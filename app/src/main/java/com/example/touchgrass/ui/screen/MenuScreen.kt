package com.example.touchgrass.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.R
import com.example.touchgrass.ui.theme.*
import com.example.touchgrass.viewmodel.ProfileViewModel

@Composable
fun MenuScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onSingleplayerClick: () -> Unit,
    onJoinMultiplayerClick: (Long) -> Unit,
    onJoinGameClick: () -> Unit,
    onCreateGameClick: () -> Unit,
    onLogout: () -> Unit
) {
    val username by profileViewModel.username.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf(username) }
    var gameCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Barre supérieure harmonisée (64dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.TopCenter)
        ) {
            // Logo à gauche
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(54.dp)
                    .scale(1.6f)
                    .align(Alignment.CenterStart)
            )

            // Titre au centre
            Text(
                text = "TouchGrass",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BackgroundMediumBlack)
                    .clickable { 
                        tempUsername = username
                        showProfileDialog = true 
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = username.ifEmpty { "Profil" },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Déconnexion",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Menu Principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuButton(
                title = "Singleplayer",
                subtitle = "Explorer le monde en solo",
                iconRes = R.drawable.iconssingleplayer,
                onClick = onSingleplayerClick
            )

            MenuButton(
                title = "Jouer une seed",
                subtitle = "Utilise un code pour jouer avec vos amis",
                iconRes = R.drawable.iconsmultiplayer,
                onClick = { showJoinDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            MenuButton(
                title = "Créer un salon",
                subtitle = "Inviter des amis en multi",
                iconRes = R.drawable.iconsadd,
                onClick = onCreateGameClick
            )

            MenuButton(
                title = "Rejoindre un salon",
                subtitle = "Saisir un code d'invitation",
                iconRes = R.drawable.iconsenter,
                onClick = onJoinGameClick
            )
        }

        // Boîte de dialogue Profil
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                containerColor = BackgroundMediumBlack,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Modifier le profil", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    Column {
                        Text("Entrez votre nouveau pseudo :", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = tempUsername,
                            onValueChange = { tempUsername = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = ScreenBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempUsername.isNotBlank()) {
                                profileViewModel.setUsername(tempUsername.trim())
                                showProfileDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("VALIDER", fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("ANNULER", color = Color.Gray)
                    }
                }
            )
        }

        // Boîte de dialogue Jouer une Seed
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                containerColor = BackgroundMediumBlack,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        "Jouer une seed",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Entrez le code à 6 chiffres :",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = gameCode,
                            onValueChange = { if (it.length <= 6) gameCode = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = ScreenBorder,
                                cursorColor = Primary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val seed = gameCode.toLongOrNull()
                            if (seed != null) {
                                onJoinMultiplayerClick(seed)
                                showJoinDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = gameCode.length == 6,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("REJOINDRE", fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("ANNULER", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun MenuButton(title: String, subtitle: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BackgroundMediumBlack)
            .border(1.dp, ScreenBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(BackgroundDeepBlack, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(id = iconRes), null, tint = Primary, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
