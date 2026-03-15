package com.abpvt.newsapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    small  = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large  = RoundedCornerShape(24.dp)
)

private val DarkColorPalette = darkColors(
    primary          = DeepBlue,
    primaryVariant   = DeepBlueDark,
    secondary        = Amber,
    secondaryVariant = AmberDark,
    background       = DarkBackground,
    surface          = DarkCard,
    onPrimary        = Color.White,
    onSecondary      = Color.Black,
    onBackground     = Color.White,
    onSurface        = Color.White,
    error            = DownvoteActive
)

private val LightColorPalette = lightColors(
    primary          = DeepBlue,
    primaryVariant   = DeepBlueDark,
    secondary        = Amber,
    secondaryVariant = AmberDark,
    background       = LightBackground,
    surface          = LightCard,
    onPrimary        = Color.White,
    onSecondary      = Color.Black,
    onBackground     = TitleText,
    onSurface        = TitleText,
    error            = DownvoteActive
)

@Composable
fun NewsappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors     = colors,
        typography = Typography,
        shapes     = AppShapes,
        content    = content
    )
}