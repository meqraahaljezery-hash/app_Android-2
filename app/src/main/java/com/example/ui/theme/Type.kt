package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.i18n.AppLanguage

// =========================================================================
// خطوط المقرأة الحديثة والذكية (Modern Advertising & Quranic Typography)
// =========================================================================

// 1. الخط العربي الإعلاني الحديث والذكي (Cairo Google Font)
val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

// 2. الخط الأمهري الإعلاني الحديث (Noto Sans Ethiopic Google Font)
val NotoSansEthiopicFontFamily = FontFamily(
    Font(R.font.noto_sans_ethiopic, FontWeight.Normal),
    Font(R.font.noto_sans_ethiopic, FontWeight.Medium),
    Font(R.font.noto_sans_ethiopic, FontWeight.SemiBold),
    Font(R.font.noto_sans_ethiopic, FontWeight.Bold)
)

// 3. خط المصحف والرسم العثماني (Uthmanic Hafs) للآيات والتلاوة
val UthmanicHafsFontFamily = FontFamily(
    Font(R.font.uthmanic_hafs, FontWeight.Normal),
    Font(R.font.uthmanic_hafs, FontWeight.Bold)
)

/**
 * تحديد عائلة الخط المناسبة ديناميكياً حسب لغة العرض الحالية
 */
fun getAppFontFamily(language: AppLanguage): FontFamily {
    return when (language) {
        AppLanguage.AMHARIC -> NotoSansEthiopicFontFamily
        AppLanguage.ARABIC -> CairoFontFamily
        AppLanguage.ENGLISH -> CairoFontFamily
    }
}

// نمط مخصص للآيات القرآنية والرسم العثماني
val QuranicTextStyle = TextStyle(
    fontFamily = UthmanicHafsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp
)

val QuranicHeadlineStyle = TextStyle(
    fontFamily = UthmanicHafsFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 48.sp,
    letterSpacing = 0.sp
)

// النمط العام للواجهات باستخدام خط كايرو الإعلاني الحديث
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)
