package com.abpvt.newsapp.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val error by viewModel.error.collectAsState()

    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleConfigured = remember(context) {
        GoogleSignInSupport.webClientId(context).isNotBlank()
    }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logo_scale"
    )

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
                    colors = listOf(GradientStart, GradientEnd, Color(0xFF1A237E))
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

            // ── Logo ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -60 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(logoScale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Amber, Color(0xFFFF6F00))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoStories,
                            contentDescription = "Newsynk",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Newsynk",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Stay synced. Stay ahead.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Input Card ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 200)) + slideInVertically(tween(800, delayMillis = 200)) { 80 }
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome back",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue
                        )
                        Text(
                            text = "Sign in to your account",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        // ── Email ─────────────────────────────────────────────
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

                        // ── Password ──────────────────────────────────────────
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                viewModel.clearError()
                            },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your password", color = Color(0xFFBBBBBB)) },
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // ── Remember Me & Forgot Password ─────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = DeepBlue,
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Keep me signed in",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF444444)
                                )
                            }
                            TextButton(
                                onClick = { navController.navigate(Screen.ForgotPassword.route) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Forgot password?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Amber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ── Error ─────────────────────────────────────────────
                        error?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // ── Sign In Button ────────────────────────────────────
                        if (isLoading) {
                            CircularProgressIndicator(color = DeepBlue)
                        } else {
                            Button(
                                onClick = { viewModel.login(email, password, rememberMe) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = email.isNotBlank() && password.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Sign In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ── OR divider ────────────────────────────────────────
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
                                            GoogleSignInSupport.idToken(context), rememberMe
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Register link ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, delayMillis = 400))
            ) {
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Register",
                        color = Amber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
