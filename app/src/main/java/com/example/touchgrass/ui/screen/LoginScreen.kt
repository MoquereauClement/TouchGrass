package com.example.touchgrass.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touchgrass.ui.theme.BackgroundDeepBlack
import com.example.touchgrass.ui.theme.Primary
import com.example.touchgrass.ui.theme.ScreenBorder
import com.example.touchgrass.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

/**
 * Écran de connexion et d'inscription.
 * Si l'email n'existe pas, un compte est créé automatiquement.
 */
@Composable
fun LoginScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    // États pour les champs de saisie
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // On utilise un scope de coroutine pour les appels asynchrones (Firebase)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // En-tête avec le nom de l'application
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "TouchGrass",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Formulaire central
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rejoignez l'aventure",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Entrez vos identifiants pour jouer.\nSi vous n'avez pas de compte, il sera créé.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Champ Email
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = null // On efface l'erreur quand l'utilisateur écrit
                },
                label = { Text("Adresse e-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = ScreenBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Champ Pseudo
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    errorMessage = null
                },
                label = { Text("Pseudo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = ScreenBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Affichage de l'erreur si elle existe
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
            }
        }

        // Bouton de connexion en bas
        Button(
            onClick = {
                if (email.isNotBlank() && username.isNotBlank()) {
                    isLoading = true
                    scope.launch {
                        // On vérifie si l'utilisateur existe déjà ou si le pseudo est pris
                        val redundancyError = profileViewModel.checkUserRedundancy(email.trim(), username.trim())
                        if (redundancyError == null) {
                            // Connexion réussie
                            profileViewModel.login(email.trim(), username.trim())
                            onLoginSuccess()
                        } else {
                            errorMessage = redundancyError
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp),
            enabled = email.isNotBlank() && username.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("SE CONNECTER / S'INSCRIRE", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
