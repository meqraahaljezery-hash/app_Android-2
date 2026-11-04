package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DeepGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = LightGreen,
    onPrimaryContainer = DeepGreen,
    secondary = GoldAccent,
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = PrimaryGreen,
    background = Color(0xFFFFFFFF), // Pure white background
    onBackground = TextPrimary,
    surface = Color(0xFFFFFFFF),    // Pure white surface
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF22C55E),
    onPrimary = Color(0xFF0B132B),
    primaryContainer = DeepGreen,
    onPrimaryContainer = LightGreen,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    tertiary = LightGreen,
    background = DarkNavyBackground, // Deep dark blue / navy
    onBackground = DarkNavyTextPrimary,
    surface = DarkNavySurface,
    onSurface = DarkNavyTextPrimary,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = DarkNavyTextSecondary,
    outline = DarkNavyBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) darkColorScheme() else lightColorScheme()
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
