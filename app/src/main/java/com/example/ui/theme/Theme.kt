package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * أنماط الثيم المتاحة للمقرأة:
 * - SYSTEM: تتبع الوضع المختار في إعدادات هاتف المستخدم تلقائياً
 * - LIGHT: الوضع النهاري الزمردي الفاخر
 * - NIGHT: الوضع الليلي والتهجد المريح للعينين والتلاوة في الظلام
 * - DYNAMIC: ألوان Material You التكيفية المستخرجة من خلفية شاشة نظام أندرويد 12+
 */
enum class AppThemeMode(val titleAr: String, val titleEn: String) {
    SYSTEM("تلقائي (حسب نظام الجوال)", "System Default"),
    LIGHT("الوضع النهاري الفاخر", "Daylight Luxury"),
    NIGHT("الوضع الليلي والتهجد", "Night Recitation"),
    DYNAMIC("ألوان النظام التكيفية (Android 12+)", "Dynamic Material You")
}

val LocalThemeMode = staticCompositionLocalOf { AppThemeMode.SYSTEM }
val LocalThemeModeController = staticCompositionLocalOf<(AppThemeMode) -> Unit> { {} }

// 1. نظام الألوان النهاري المتقن مع تدرجات الحاويات
private val LightColorScheme = lightColorScheme(
    primary = IslamicEmerald,
    onPrimary = Color.White,
    primaryContainer = IslamicEmeraldContainer,
    onPrimaryContainer = IslamicEmeraldDark,
    inversePrimary = IslamicGoldBright,
    secondary = IslamicGold,
    onSecondary = Color.White,
    secondaryContainer = IslamicGoldContainer,
    onSecondaryContainer = IslamicAmber,
    tertiary = PersianTurquoise,
    onTertiary = Color.White,
    tertiaryContainer = PersianTurquoiseLight,
    onTertiaryContainer = Color(0xFF0C4A6E),
    background = LuxuryBackground,
    onBackground = TextPrimaryDark,
    surface = LuxurySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = IslamicEmeraldSubtle,
    onSurfaceVariant = TextSecondaryMuted,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurfaceBright,
    inverseSurface = MosqueNightSurface,
    inverseOnSurface = TextNightPrimary,
    outline = SurfaceBorderDecor,
    outlineVariant = Color(0xFFF1F5F9),
    error = StatusLiveCrimson,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

// 2. نظام الألوان الليلي (Mosque Night Mode) المخصص للقراءة الليلية والتهجد
// يتميز بأسطح متدرجة بدقة تمنع إجهاد العين وتلائم شاشات OLED
private val DarkColorScheme = darkColorScheme(
    primary = IslamicGoldBright,
    onPrimary = Color(0xFF041510),
    primaryContainer = MosqueNightSurfaceContainerHigh,
    onPrimaryContainer = Color(0xFFFEF08A),
    inversePrimary = IslamicEmerald,
    secondary = IslamicGold,
    onSecondary = Color(0xFF1C1917),
    secondaryContainer = Color(0xFF2C220E),
    onSecondaryContainer = Color(0xFFFEF9C3),
    tertiary = PersianTurquoise,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0C4A6E),
    onTertiaryContainer = Color(0xFFBAE6FD),
    background = MosqueNightBackground,
    onBackground = TextNightPrimary,
    surface = MosqueNightSurface,
    onSurface = TextNightPrimary,
    surfaceVariant = MosqueNightCard,
    onSurfaceVariant = TextNightSecondary,
    surfaceContainerLowest = MosqueNightSurfaceLowest,
    surfaceContainerLow = MosqueNightSurfaceContainerLow,
    surfaceContainer = MosqueNightSurfaceContainer,
    surfaceContainerHigh = MosqueNightSurfaceContainerHigh,
    surfaceContainerHighest = MosqueNightSurfaceContainerHighest,
    surfaceDim = MosqueNightSurfaceDim,
    surfaceBright = MosqueNightSurfaceBright,
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF0F172A),
    outline = MosqueNightBorder,
    outlineVariant = MosqueNightBorderVariant,
    error = StatusLiveCrimson,
    onError = Color.White,
    errorContainer = Color(0xFF5F1414),
    onErrorContainer = Color(0xFFFECACA)
)

/**
 * دمج الألوان الديناميكية من نظام أندرويد 12+ مع تدرجات الحاويات الليلية لمنع التوهج
 */
@Composable
fun buildDynamicDarkColorScheme(context: Context): ColorScheme {
    val dynamic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        DarkColorScheme
    }
    return dynamic.copy(
        background = MosqueNightBackground,
        surface = MosqueNightSurface,
        surfaceVariant = MosqueNightCard,
        surfaceContainerLowest = MosqueNightSurfaceLowest,
        surfaceContainerLow = MosqueNightSurfaceContainerLow,
        surfaceContainer = MosqueNightSurfaceContainer,
        surfaceContainerHigh = MosqueNightSurfaceContainerHigh,
        surfaceContainerHighest = MosqueNightSurfaceContainerHighest,
        surfaceDim = MosqueNightSurfaceDim,
        surfaceBright = MosqueNightSurfaceBright,
        outline = MosqueNightBorder,
        outlineVariant = MosqueNightBorderVariant
    )
}

/**
 * دمج الألوان الديناميكية النهارية مع درجات الحاويات الفاخرة
 */
@Composable
fun buildDynamicLightColorScheme(context: Context): ColorScheme {
    val dynamic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context)
    } else {
        LightColorScheme
    }
    return dynamic.copy(
        background = LuxuryBackground,
        surface = LuxurySurface,
        surfaceContainerLowest = LightSurfaceContainerLowest,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        surfaceContainerHighest = LightSurfaceContainerHighest,
        surfaceDim = LightSurfaceDim,
        surfaceBright = LightSurfaceBright,
        outline = SurfaceBorderDecor
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val isEffectivelyDark = when (themeMode) {
        AppThemeMode.NIGHT -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM, AppThemeMode.DYNAMIC -> darkTheme
    }

    val isDynamicActive = (dynamicColor || themeMode == AppThemeMode.DYNAMIC) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        isDynamicActive && isEffectivelyDark -> buildDynamicDarkColorScheme(context)
        isDynamicActive && !isEffectivelyDark -> buildDynamicLightColorScheme(context)
        isEffectivelyDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
