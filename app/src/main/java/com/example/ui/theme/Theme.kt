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
    primary = IslamicEmerald,
    onPrimary = Color.White,
    primaryContainer = IslamicEmeraldContainer,
    onPrimaryContainer = IslamicEmerald,
    secondary = IslamicGold,
    onSecondary = Color.White,
    secondaryContainer = IslamicGoldContainer,
    onSecondaryContainer = IslamicAmber,
    tertiary = PersianTurquoise,
    onTertiary = Color.White,
    background = ParchmentBackground,
    onBackground = TextPrimaryDark,
    surface = ParchmentSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = IslamicEmeraldSubtle,
    onSurfaceVariant = TextSecondaryMuted,
    outline = IslamicGoldBorder,
    error = StatusLiveCrimson
)

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGoldBright,
    onPrimary = Color.Black,
    primaryContainer = MosqueNightCard,
    onPrimaryContainer = IslamicGoldBright,
    secondary = PersianTurquoise,
    onSecondary = Color.White,
    tertiary = IslamicEmeraldLight,
    background = MosqueNightBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = MosqueNightSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MosqueNightCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = MosqueNightBorder,
    error = StatusLiveCrimson
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
