package com.abpvt.newsapp.auth

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavHostController
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.theme.Amber
import com.abpvt.newsapp.ui.theme.DeepBlue
import com.abpvt.newsapp.ui.theme.GradientEnd
import com.abpvt.newsapp.ui.theme.GradientStart
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleConfigured = remember(context) {
        GoogleSignInSupport.webClientId(context).isNotBlank()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                googleLoading = false
                navController.navigate(Screen.News.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is AuthState.Error -> googleLoading = false
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd, Color(0xFF0B1528))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 28.dp)
                .padding(top = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NEWSYNK  /  JOIN THE CONVERSATION",
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = Amber
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "A better way to stay informed.",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Create your free account to shape your daily briefing.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
            )

            Card(
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Full Name ─────────────────────────────────────────────
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.clearError()
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. John Smith", color = Color(0xFFBBBBBB)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = DeepBlue)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepBlue,
                            unfocusedBorderColor = Color(0xFFDDE3F5),
                            cursorColor = DeepBlue,
                            focusedLabelColor = DeepBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = Color(0xFFF8F9FF),
                            focusedContainerColor = Color(0xFFF8F9FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Email ─────────────────────────────────────────────────
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearError()
                        },
                        label = { Text("Email address") },
                        placeholder = { Text("e.g. john@example.com", color = Color(0xFFBBBBBB)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = DeepBlue)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepBlue,
                            unfocusedBorderColor = Color(0xFFDDE3F5),
                            cursorColor = DeepBlue,
                            focusedLabelColor = DeepBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = Color(0xFFF8F9FF),
                            focusedContainerColor = Color(0xFFF8F9FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Password ──────────────────────────────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearError()
                        },
                        label = { Text("Password") },
                        placeholder = { Text("At least 6 characters", color = Color(0xFFBBBBBB)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DeepBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide" else "Show",
                                    tint = Color.Gray
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepBlue,
                            unfocusedBorderColor = Color(0xFFDDE3F5),
                            cursorColor = DeepBlue,
                            focusedLabelColor = DeepBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = Color(0xFFF8F9FF),
                            focusedContainerColor = Color(0xFFF8F9FF)
                        )
                    )

                    // ── Error ─────────────────────────────────────────────────
                    error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Register Button ───────────────────────────────────────
                    Button(
                        onClick = { viewModel.register(name, email, password) },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.length >= 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create my account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // ── OR divider ────────────────────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                        Text(text = "  OR  ", color = Color.Gray, fontSize = 12.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Continue with Google ──────────────────────────────
                    OutlinedButton(
                        onClick = {
                            googleLoading = true
                            viewModel.clearError()
                            coroutineScope.launch {
                                try {
                                    viewModel.signInWithGoogle(
                                        GoogleSignInSupport.idToken(context), true
                                    )
                                } catch (e: GetCredentialCancellationException) {
                                    googleLoading = false
                                } catch (e: NoCredentialException) {
                                    googleLoading = false
                                    viewModel.setExternalError("No Google account is available on this device.")
                                } catch (e: Exception) {
                                    googleLoading = false
                                    val msg = e.message ?: "Google Sign-In failed"
                                    viewModel.setExternalError(msg)
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = googleConfigured && !googleLoading && !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        if (googleLoading) {
                            CircularProgressIndicator(
                                color = DeepBlue,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "G",
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (!googleConfigured) {
                        Text(
                            "Google sign-in is unavailable in this build. Add Firebase configuration to enable it.",
                            color = Color(0xFF5F6677),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // ── Privacy Policy notice ─────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "By creating an account, you agree to our",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Privacy Policy",
                color = Amber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://not-a-hack-er.github.io/newsynk-privacy-policy/"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = "Already have an account? ",
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "Sign In",
                    color = Amber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
