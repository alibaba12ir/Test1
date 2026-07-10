package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DuoGreen,
    secondary = DuoBlue,
    tertiary = DuoOrange,
    background = DuoBg,
    surface = DuoCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DuoTextPrimary,
    onSurface = DuoTextPrimary,
    primaryContainer = DuoDarkGreen,
    secondaryContainer = DuoDarkBlue,
    tertiaryContainer = DuoDarkOrange
)

private val LightColorScheme = lightColorScheme(
    primary = DuoGreen,
    secondary = DuoBlue,
    tertiary = DuoOrange,
    background = Color(0xFFF7F7F7),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF3C3C3C),
    onSurface = Color(0xFF3C3C3C),
    primaryContainer = Color(0xFFE8F9E0),
    secondaryContainer = Color(0xFFE1F5FE),
    tertiaryContainer = Color(0xFFFFF3E0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false so the Duolingo theme is guaranteed and cohesive
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
