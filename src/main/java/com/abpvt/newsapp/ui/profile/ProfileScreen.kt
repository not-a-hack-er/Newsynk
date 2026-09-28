package com.abpvt.newsapp.ui.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.abpvt.newsapp.auth.AuthState
import com.abpvt.newsapp.auth.AuthViewModel
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.notifications.NotificationsPrefs
import com.abpvt.newsapp.ui.theme.*
import com.abpvt.newsapp.ui.components.PremiumBottomBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ── Avatar palette ────────────────────────────────────────────────────────────
private data class AvatarPalette(val start: Color, val end: Color)
private val avatarPalettes = listOf(
    AvatarPalette(Color(0xFF1565C0), Color(0xFF42A5F5)),
    AvatarPalette(Color(0xFF6A1B9A), Color(0xFFCE93D8)),
    AvatarPalette(Color(0xFF00695C), Color(0xFF4DB6AC)),
    AvatarPalette(Color(0xFFE65100), Color(0xFFFFCC80)),
    AvatarPalette(Color(0xFFAD1457), Color(0xFFF48FB1)),
    AvatarPalette(Color(0xFFB71C1C), Color(0xFFEF9A9A)),
)

// ── Notification row model ────────────────────────────────────────────────────
private data class NotifCategory(
    val label: String, val emoji: String, val description: String,
    val prefKey: String, val topic: String
)
private val notifCategories = listOf(
    NotifCategory("Breaking News",   "🔥", "Major urgent alerts",             NotificationsPrefs.KEY_NOTIF_BREAKING,     NotificationsPrefs.TOPIC_BREAKING_NEWS),
    NotifCategory("Sports",          "🏆", "Match results & live scores",     NotificationsPrefs.KEY_NOTIF_SPORTS,       NotificationsPrefs.TOPIC_SPORTS),
    NotifCategory("Technology",      "💻", "Tech launches & AI updates",      NotificationsPrefs.KEY_NOTIF_TECH,         NotificationsPrefs.TOPIC_TECH),
    NotifCategory("Business",        "💼", "Markets, finance & economy",      NotificationsPrefs.KEY_NOTIF_BUSINESS,     NotificationsPrefs.TOPIC_BUSINESS),
    NotifCategory("Health",          "🏥", "Health, science & wellness",      NotificationsPrefs.KEY_NOTIF_HEALTH,       NotificationsPrefs.TOPIC_HEALTH),
    NotifCategory("World News",      "🌍", "Global events & politics",        NotificationsPrefs.KEY_NOTIF_WORLD,        NotificationsPrefs.TOPIC_WORLD),
    NotifCategory("Entertainment",   "🎬", "Movies, music & celebrity news",  NotificationsPrefs.KEY_NOTIF_ENTERTAINMENT, NotificationsPrefs.TOPIC_ENTERTAINMENT),
    NotifCategory("Comment Replies", "💬", "Replies to your comments",        NotificationsPrefs.KEY_NOTIF_COMMENTS,     NotificationsPrefs.TOPIC_COMMENTS),
    NotifCategory("Daily Digest",    "☀️", "Personalized briefing at your chosen time", NotificationsPrefs.KEY_NOTIF_DIGEST, ""),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val context = LocalContext.current

    val isLoading            by authViewModel.isLoading.collectAsState()
    val profileUpdateSuccess by authViewModel.profileUpdateSuccess.collectAsState()
    val authState            by authViewModel.authState.collectAsState()
    val currentUserName      by authViewModel.currentUserName.collectAsState()
    val profilePhotoUri      by authViewModel.profilePhotoUri.collectAsState()
    val avatarColorLong      by authViewModel.avatarColor.collectAsState()
    val quietHoursEnabled    by authViewModel.quietHoursEnabled.collectAsState()
    val quietStartHour       by authViewModel.quietStartHour.collectAsState()
    val quietEndHour         by authViewModel.quietEndHour.collectAsState()
    val digestHour           by authViewModel.digestHour.collectAsState()
    val analyticsConsent     by authViewModel.analyticsConsent.collectAsState()

    val notifStates: Map<String, Boolean> = mapOf(
        NotificationsPrefs.KEY_NOTIF_BREAKING      to authViewModel.notifBreaking.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_SPORTS        to authViewModel.notifSports.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_TECH          to authViewModel.notifTech.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_BUSINESS      to authViewModel.notifBusiness.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_HEALTH        to authViewModel.notifHealth.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_WORLD         to authViewModel.notifWorld.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_ENTERTAINMENT to authViewModel.notifEntertainment.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_COMMENTS      to authViewModel.notifComments.collectAsState().value,
        NotificationsPrefs.KEY_NOTIF_DIGEST        to authViewModel.notifDigest.collectAsState().value,
    )

    val firebaseUser     = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val currentUserEmail = firebaseUser?.email ?: "Unknown Email"
    val memberSince = remember(firebaseUser) {
        val ts = firebaseUser?.metadata?.creationTimestamp ?: 0L
        if (ts > 0L) SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(ts)) else ""
    }

    val selectedPaletteIdx = remember(avatarColorLong) {
        avatarPalettes.indexOfFirst { it.start.toArgb().toLong() == avatarColorLong }.takeIf { it >= 0 } ?: 0
    }
    val palette = avatarPalettes[selectedPaletteIdx]

    var isEditingName   by remember { mutableStateOf(false) }
    var editNameInput   by remember { mutableStateOf("") }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var isLoggingOut    by remember { mutableStateOf(false) }
    var visible         by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(50); visible = true }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            authViewModel.setNotificationCategory(
                NotificationsPrefs.KEY_NOTIF_BREAKING, NotificationsPrefs.TOPIC_BREAKING_NEWS, true
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            authViewModel.setProfilePhoto(uri)
        }
    }

    LaunchedEffect(profileUpdateSuccess) {
        if (profileUpdateSuccess) { isEditingName = false; authViewModel.resetProfileUpdateState() }
    }
    LaunchedEffect(authState) {
        if (isLoggingOut && authState is AuthState.Unauthenticated) {
            isLoggingOut = false
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue)
            )
        },
        bottomBar = { PremiumBottomBar(navController, Screen.Profile.route) }
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

            // ── Avatar ───────────────────────────────────────────────────────
            AnimatedVisibility(visible, enter = fadeIn(tween(600)) + scaleIn(tween(600))) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(110.dp).clip(CircleShape).clickable { showAvatarSheet = true }
                            .background(Brush.linearGradient(listOf(palette.start, palette.end))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUri != null) {
                            AsyncImage(
                                model = profilePhotoUri, contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = (currentUserName.firstOrNull()?.uppercaseChar() ?: 'N').toString(),
                                fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable { showAvatarSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("Tap to change photo or colour", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(20.dp))

            // ── Account Details ──────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Account Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Name:", fontWeight = FontWeight.Medium)
                        if (isEditingName) {
                            OutlinedTextField(
                                value = editNameInput, onValueChange = { editNameInput = it },
                                modifier = Modifier.weight(1f).padding(start = 12.dp), singleLine = true,
                                trailingIcon = {
                                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    else Row {
                                        TextButton(onClick = { isEditingName = false }) { Text("Cancel") }
                                        TextButton(
                                            onClick = { authViewModel.updateUsername(editNameInput) },
                                            enabled = editNameInput.isNotBlank()
                                        ) { Text("Save") }
                                    }
                                }
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentUserName.ifBlank { "No Name Set" }, color = Color.Gray)
                                IconButton(
                                    onClick = { editNameInput = currentUserName; isEditingName = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Email:", fontWeight = FontWeight.Medium)
                        Text(currentUserEmail, color = Color.Gray, modifier = Modifier.padding(end = 6.dp))
                    }
                    if (memberSince.isNotBlank()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Joined:", fontWeight = FontWeight.Medium)
                            Text("Member since $memberSince", color = Color.Gray, modifier = Modifier.padding(end = 6.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Notifications Card ───────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔔 Notifications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Choose what you want to be notified about",
                        style = MaterialTheme.typography.bodySmall, color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))

                    notifCategories.forEach { cat ->
                        val isEnabled = notifStates[cat.prefKey] ?: true
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${cat.emoji} ${cat.label}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(cat.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    authViewModel.setNotificationCategory(cat.prefKey, cat.topic, enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White, checkedTrackColor = DeepBlue,
                                    uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.Gray
                                )
                            )
                        }
                        if (cat != notifCategories.last()) HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                    }

                    AnimatedVisibility(
                        visible = notifStates[NotificationsPrefs.KEY_NOTIF_DIGEST] == true
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Daily briefing time", fontWeight = FontWeight.SemiBold)
                                Text("Built from your followed topics", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { authViewModel.setDigestHour(digestHour - 1) },
                                        modifier = Modifier.size(36.dp)
                                    ) { Text("‹", fontSize = 20.sp) }
                                    Text(String.format("%02d:00", digestHour), fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = { authViewModel.setDigestHour(digestHour + 1) },
                                        modifier = Modifier.size(36.dp)
                                    ) { Text("›", fontSize = 20.sp) }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(10.dp))

                    // Quiet Hours
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("🌙 Quiet Hours", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Mute all notifications during set hours", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = quietHoursEnabled,
                            onCheckedChange = { authViewModel.setQuietHoursEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = DeepBlue,
                                uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    AnimatedVisibility(visible = quietHoursEnabled) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("From", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { authViewModel.setQuietStartHour((quietStartHour - 1 + 24) % 24) }, modifier = Modifier.size(36.dp)) {
                                                Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(
                                                text = String.format("%02d:00", quietStartHour),
                                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp
                                            )
                                            IconButton(onClick = { authViewModel.setQuietStartHour((quietStartHour + 1) % 24) }, modifier = Modifier.size(36.dp)) {
                                                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Until", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { authViewModel.setQuietEndHour((quietEndHour - 1 + 24) % 24) }, modifier = Modifier.size(36.dp)) {
                                                Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(
                                                text = String.format("%02d:00", quietEndHour),
                                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp
                                            )
                                            IconButton(onClick = { authViewModel.setQuietEndHour((quietEndHour + 1) % 24) }, modifier = Modifier.size(36.dp)) {
                                                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "No notifications from ${String.format("%02d:00", quietStartHour)} to ${String.format("%02d:00", quietEndHour)}",
                                style = MaterialTheme.typography.bodySmall, color = Color.Gray,
                                modifier = Modifier.padding(top = 6.dp).align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = DeepBlue)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Help improve Newsynk", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Share anonymous usage analytics. Off by default.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = analyticsConsent,
                        onCheckedChange = authViewModel::setAnalyticsConsent
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Dark Mode ────────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isDarkMode) Icons.Default.Brightness2 else Icons.Default.BrightnessHigh,
                            null, tint = if (isDarkMode) Amber else DeepBlue, modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isDarkMode) "Dark Mode" else "Light Mode",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                if (isDarkMode) "Tap to switch to light" else "Tap to switch to dark",
                                style = MaterialTheme.typography.bodySmall, color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = isDarkMode, onCheckedChange = { onToggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White, checkedTrackColor = DeepBlue,
                            uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Bookmarks ────────────────────────────────────────────────────
            Button(
                onClick = { navController.navigate(Screen.Bookmarks.route) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = AmberDark, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Star, null, modifier = Modifier.padding(end = 8.dp))
                Text("View Saved Articles", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // ── Contact Us ───────────────────────────────────────────────────
            Button(
                onClick = { navController.navigate(Screen.Contact.route) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue.copy(alpha = 0.85f), contentColor = Color.White)
            ) {
                Icon(Icons.Default.Email, null, modifier = Modifier.padding(end = 8.dp))
                Text("Contact Us", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { isLoggingOut = true; authViewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Avatar Sheet ─────────────────────────────────────────────────────────
    if (showAvatarSheet) {
        AlertDialog(
            onDismissRequest = { showAvatarSheet = false },
            title = { Text("Change Avatar", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextButton(
                        onClick = { showAvatarSheet = false; photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Choose from Gallery", style = MaterialTheme.typography.bodyLarge)
                    }
                    if (profilePhotoUri != null) {
                        TextButton(
                            onClick = { authViewModel.clearProfilePhoto(); showAvatarSheet = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 8.dp))
                            Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Avatar Colour", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(8.dp))
                    avatarPalettes.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            row.forEach { p ->
                                val selected = p == avatarPalettes[selectedPaletteIdx]
                                Box(
                                    modifier = Modifier
                                        .size(48.dp).clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(p.start, p.end)))
                                        .then(if (selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                                        .clickable { authViewModel.setAvatarColor(p.start.toArgb().toLong()); showAvatarSheet = false }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAvatarSheet = false }) { Text("Close") } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
