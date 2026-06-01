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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barre supérieure
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(BackgroundMediumBlack, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }

            Text(
                text = "Rejoindre",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = "Entrez la clé de la partie fournie par l'hôte.",
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        )

        // Champ de saisie du code
        OutlinedTextField(
            value = gameCode,
            onValueChange = { if (it.length <= 6) gameCode = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            placeholder = {
                Text(
                    "Entre la clé",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.qr_code),
                    contentDescription = "Scanner QR",
                    tint = IconsColor,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp) // Pour atteindre 20dp de padding (12dp par défaut + 8dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BackgroundMediumBlack,
                unfocusedContainerColor = BackgroundMediumBlack,
                focusedBorderColor = ScreenBorder,
                unfocusedBorderColor = ScreenBorder,
                cursorColor = Primary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton Confirmer
        Button(
            onClick = {
                gameCode.toLongOrNull()?.let { onJoinGame(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Primary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = gameCode.length == 6
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confirmer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
