package com.abpvt.newsapp.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.theme.Amber
import com.abpvt.newsapp.ui.theme.DeepBlue
import com.abpvt.newsapp.ui.theme.GradientEnd
import com.abpvt.newsapp.ui.theme.GradientStart
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val resetEmailSent by viewModel.resetEmailSent.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.resetPasswordState()
        delay(100)
        visible = true
    }

    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "icon_scale"
    )

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

            // ── Icon ────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -60 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(iconScale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Amber, Color(0xFFFF6F00))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (resetEmailSent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Text(text = "🔒", fontSize = 36.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Reset Password",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "We'll help you get back in",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Input Card ──────────────────────────────────────────────────
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

                        if (resetEmailSent) {
                            // Success state
                            Text(
                                text = "Check your inbox",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "We've sent a password reset link to:\n$email",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Back to Sign In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Input state
                            Text(
                                text = "Forgot your password?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue
                            )
                            Text(
                                text = "Enter your email address to receive a link.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    if (error != null) viewModel.clearError()
                                },
                                label = { Text("Email") },
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
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )

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

                            if (isLoading) {
                                CircularProgressIndicator(color = DeepBlue)
                            } else {
                                Button(
                                    onClick = { viewModel.sendPasswordReset(email) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = email.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DeepBlue,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Send Reset Link",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Back button
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, delayMillis = 400))
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        text = "Remembered it? ",
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
}
