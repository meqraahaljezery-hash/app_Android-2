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
// خطوط المقرأة الحديثة والفاخرة (Modern Luxury & Quranic Typography)
// =========================================================================

// 1. خط "تجوال" الإعلاني الحديث للواجهات والعناوين والأزرار (Tajawal Font)
val TajawalFontFamily = FontFamily(
    Font(R.font.tajawal, FontWeight.Normal),
    Font(R.font.tajawal, FontWeight.Medium),
    Font(R.font.tajawal, FontWeight.SemiBold),
    Font(R.font.tajawal, FontWeight.Bold)
)

// خط "كايرو" الإعلاني الحديث كبديل إضافي
val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

// 2. الخطوط الأمهرية المعتمدة (Zelan, Adwa, WashRa Fonts)
// أ) خط "ዘላን" (Ethiopic Zelan Font) - الخط الأمهري الإعلاني العصري النظيف
val ZelanFontFamily = FontFamily(
    Font(R.font.zelan, FontWeight.Normal),
    Font(R.font.zelan, FontWeight.Bold)
)

// ب) خط "አድዋ" (Ethiopic Adwa Font) - خط أمهري متميز ومناسب للعناوين والشاشات
val AdwaFontFamily = FontFamily(
    Font(R.font.adwa, FontWeight.Normal),
    Font(R.font.adwa_bold, FontWeight.Bold)
)

// ج) خط "ዋሽራ" (Ethiopic WashRa Font) - خط المخطوطات والوثائق الأمهرية الرسمية
val WashraFontFamily = FontFamily(
    Font(R.font.washra, FontWeight.Normal),
    Font(R.font.washra, FontWeight.Bold)
)

// إبقاء NotoSansEthiopicFontFamily متوافقاً وموجهاً لخط زيلان وأدوا
val NotoSansEthiopicFontFamily = ZelanFontFamily

// 3. خط المصحف والرسم العثماني (Uthmanic Hafs) للآيات والتلاوة
val UthmanicHafsFontFamily = FontFamily(
    Font(R.font.uthmanic_hafs, FontWeight.Normal),
    Font(R.font.uthmanic_hafs, FontWeight.Bold)
)

// 4. خط "أميري" القرآني (Amiri Quran Font) للنصوص القرآنية والأذكار
val AmiriQuranFontFamily = FontFamily(
    Font(R.font.amiri_quran, FontWeight.Normal),
    Font(R.font.amiri_quran, FontWeight.Bold)
)

/**
 * تحديد عائلة الخط المناسبة ديناميكياً حسب لغة العرض الحالية
 */
fun getAppFontFamily(language: AppLanguage): FontFamily {
    return when (language) {
        AppLanguage.AMHARIC -> ZelanFontFamily
        AppLanguage.ARABIC -> TajawalFontFamily
        AppLanguage.ENGLISH -> TajawalFontFamily
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

// نمط مخصص للأذكار والحديث النبوي الشريف
val AzkarTextStyle = TextStyle(
    fontFamily = AmiriQuranFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

// النمط العام للواجهات باستخدام خط "تجوال" الفاخر
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)
