package com.abpvt.newsapp.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.abpvt.newsapp.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(navController: NavHostController) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Contact Us", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(visible, enter = fadeIn(tween(600)) + scaleIn(tween(600))) {
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible, enter = fadeIn(tween(700))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "We'd love to hear from you",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Reach out for support, feedback, or partnerships",
                        style = MaterialTheme.typography.bodyMedium, color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Contact Information Card ─────────────────────────────────────
            AnimatedVisibility(visible, enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 4 }) {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Contact Information",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))

                        ContactRow(icon = Icons.Default.Email, label = "Email", value = "letnewsynk@gmail.com",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:letnewsynk@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Newsynk App Support")
                                }
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        ContactRow(icon = Icons.Default.Person, label = "Developer", value = "Akarsh Bajpai", onClick = null)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        ContactRow(icon = Icons.Default.Newspaper, label = "App", value = "Newsynk – News Aggregator", onClick = null)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        ContactRow(icon = Icons.Default.Schedule, label = "Response Time", value = "Within 48 hours", onClick = null)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        ContactRow(
                            icon = Icons.Default.Policy, label = "Privacy Policy", value = "View our Privacy Policy",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://not-a-hack-er.github.io/newsynk-privacy-policy/"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── About the App Card ───────────────────────────────────────────
            AnimatedVisibility(visible, enter = fadeIn(tween(900)) + slideInVertically(tween(900)) { it / 4 }) {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "About Newsynk",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Newsynk is a news aggregator app that curates the latest headlines from trusted publishers across the web. All articles are sourced from verified news outlets via NewsAPI.org. We do not create or own any news content — every article links directly to its original publisher.",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        Text("News Sources", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "All news content is provided by the original publishers shown on each article card. Newsynk aggregates content from NewsAPI.org and does not endorse or affiliate with any individual news source.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Send Email CTA ───────────────────────────────────────────────
            AnimatedVisibility(visible, enter = fadeIn(tween(1000))) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:letnewsynk@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Newsynk App Support")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Email Us", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
