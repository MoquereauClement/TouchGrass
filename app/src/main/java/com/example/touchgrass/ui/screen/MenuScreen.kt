package com.example.touchgrass.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
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
    var showJoinDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf(username) }
    var gameCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Barre supérieure alignée horizontalement et verticalement
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
                    .size(80.dp)
                    .align(Alignment.CenterStart)
            )

            // Titre au centre
            Text(
                text = "TouchGrass",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center)
            )

            // Profil à droite
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BackgroundMediumBlack)
                    .clickable { showProfileDialog = true }
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
            verticalArrangement = Arrangement.Center
        ) {
            MenuButton(
                title = "Singleplayer",
                subtitle = "Explorer le monde tout seul",
                iconRes = R.drawable.iconssingleplayer,
                onClick = onSingleplayerClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                title = "Jouer une seed",
                subtitle = "Utilise un code pour jouer avec vos amis",
                iconRes = R.drawable.iconsmultiplayer,
                onClick = { showJoinDialog = true }
            )

            // Grand spacer entre les 2 groupes
            Spacer(modifier = Modifier.height(48.dp))

            MenuButton(
                title = "Créer une partie",
                subtitle = "Générer un code pour inviter",
                iconRes = R.drawable.iconsadd,
                onClick = onCreateGameClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                title = "Rejoindre une partie",
                subtitle = "Saisir un code d'invitation",
                iconRes = R.drawable.iconsenter,
                onClick = onJoinGameClick
            )
        }

        // Username Dialog
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                containerColor = BackgroundMediumBlack,
                title = { Text("Votre Nom de Joueur", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    OutlinedTextField(
                        value = tempUsername,
                        onValueChange = { tempUsername = it },
                        label = { Text("Pseudo") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = ScreenBorder,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempUsername.isNotBlank()) {
                                profileViewModel.setUsername(tempUsername.trim())
                                showProfileDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = tempUsername.isNotBlank()
                    ) {
                        Text("VALIDER", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Join Game Dialog
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                containerColor = BackgroundMediumBlack,
                title = {
                    Text(
                        "Jouer une seed",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
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
                                fontWeight = FontWeight.Bold,
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
                        Text("REJOINDRE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("ANNULER", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun MenuButton(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundMediumBlack)
            .border(1.dp, ScreenBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Background, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = IconsColor,
                fontSize = 12.sp
            )
        }
    }
}
