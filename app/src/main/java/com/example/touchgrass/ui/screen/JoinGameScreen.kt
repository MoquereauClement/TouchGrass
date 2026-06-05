package com.example.touchgrass.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.touchgrass.R
import com.example.touchgrass.ui.theme.*

/**
 * Écran pour rejoindre un salon multijoueur via un code à 6 chiffres.
 */
@Composable
fun JoinGameScreen(
    onBack: () -> Unit,
    onJoinGame: (Long) -> Unit
) {
    var gameCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barre supérieure avec bouton retour
        Box(
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .background(BackgroundMediumBlack.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White)
            }

            Text(
                text = "Rejoindre",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Entrez la clé de la partie fournie par l'hôte.",
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Champ de saisie optimisé pour les chiffres
        OutlinedTextField(
            value = gameCode,
            onValueChange = { if (it.length <= 6) gameCode = it },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(20.dp),
            placeholder = {
                Text("6 CHIFFRES", color = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BackgroundMediumBlack,
                unfocusedContainerColor = BackgroundMediumBlack,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bouton de validation
        Button(
            onClick = {
                gameCode.toLongOrNull()?.let { onJoinGame(it) }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp),
            enabled = gameCode.length == 6
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("REJOINDRE LA PARTIE", fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}
