package com.example.touchgrass.ui.screen

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
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

    // Animation de zoom lent pour le fond (effet Ken Burns)
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // --- FOND DYNAMIQUE (Geoguessr Style) ---
        val context = LocalContext.current
        val imageLoader = ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1920&q=80")
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .blur(1.dp)
        )

        // Overlay dégradé pour garantir la lisibilité
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            BackgroundDeepBlack.copy(alpha = 0.7f),
                            BackgroundDeepBlack
                        )
                    )
                )
        )

        // --- CONTENU ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Barre supérieure
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(54.dp)
                        .scale(1.6f)
                        .align(Alignment.CenterStart)
                )

                Text(
                    text = "TouchGrass",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.Center)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
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

            Spacer(modifier = Modifier.weight(1f))

            // Liste des 4 boutons (Layout original conservé)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuButton(
                    title = "Singleplayer",
                    subtitle = "Explorer le monde en solo",
                    iconRes = R.drawable.iconssingleplayer,
                    onClick = onSingleplayerClick,
                    isMain = true
                )

                MenuButton(
                    title = "Jouer une seed",
                    subtitle = "Utiliser un code pour une partie fixe",
                    iconRes = R.drawable.iconsmultiplayer,
                    onClick = { showJoinDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuButton(
                    title = "Créer un salon",
                    subtitle = "Inviter des amis en multijoueur",
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
            
            Spacer(modifier = Modifier.weight(1f))
        }

        // Popup pour modifier son pseudonyme
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                containerColor = BackgroundMediumBlack,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Modifier le profil", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    OutlinedTextField(
                        value = tempUsername,
                        onValueChange = { tempUsername = it },
                        label = { Text("Nouveau pseudo") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Secondaire
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
                        colors = ButtonDefaults.buttonColors(containerColor = Secondaire)
                    ) { Text("VALIDER", color = Color.Black, fontWeight = FontWeight.Bold) }
                }
            )
        }

        // Popup pour entrer une seed manuellement
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                containerColor = BackgroundMediumBlack,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Jouer une seed", color = Color.White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Entrez le code à 6 chiffres :", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = gameCode,
                            onValueChange = { if (it.length <= 6) gameCode = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Secondaire)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            gameCode.toLongOrNull()?.let { 
                                onJoinMultiplayerClick(it)
                                showJoinDialog = false 
                            }
                        },
                        enabled = gameCode.length == 6,
                        colors = ButtonDefaults.buttonColors(containerColor = Secondaire)
                    ) { Text("REJOINDRE", color = Color.Black, fontWeight = FontWeight.Bold) }
                }
            )
        }
    }
}

/**
 * Composant réutilisable pour les boutons du menu avec effet Glassmorphism.
 */
@Composable
fun MenuButton(
    title: String, 
    subtitle: String, 
    iconRes: Int, 
    onClick: () -> Unit, 
    isMain: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isMain) Secondaire.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = if (isMain) Secondaire.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (isMain) Secondaire.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f), 
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(id = iconRes), 
                null, 
                tint = if (isMain) Secondaire else Color.White, 
                modifier = Modifier.size(26.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}
