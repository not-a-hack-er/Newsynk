package com.abpvt.newsapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── M3 Color Schemes ─────────────────────────────────────────────────────────

private val AppShapes = Shapes(
    small  = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large  = RoundedCornerShape(24.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary          = DeepBlue,
    onPrimary        = Color.White,
    primaryContainer = DeepBlueDark,
    secondary        = Amber,
    onSecondary      = Color.Black,
    secondaryContainer = AmberDark,
    background       = DarkBackground,
    onBackground     = Color.White,
    surface          = DarkCard,
    onSurface        = Color.White,
    surfaceVariant   = DarkCard,
    error            = DownvoteActive,
    onError          = Color.White,
    outline          = Color(0xFF3A3A5C),
    outlineVariant   = Color(0xFF2E2E4A)
)

private val LightColorScheme = lightColorScheme(
    primary          = DeepBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    secondary        = Amber,
    onSecondary      = Color.Black,
    secondaryContainer = Color(0xFFFFE0A0),
    background       = LightBackground,
    onBackground     = TitleText,
    surface          = LightCard,
    onSurface        = TitleText,
    surfaceVariant   = Color(0xFFEFF2FA),
    error            = DownvoteActive,
    onError          = Color.White,
    outline          = Color(0xFFDDE3F5),
    outlineVariant   = Color(0xFFE8EDF5)
)

@Composable
fun NewsappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}