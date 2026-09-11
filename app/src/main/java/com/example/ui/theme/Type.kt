package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * KAARIGAR Typography System
 * • Poppins for Modern English typography
 * • Noto Sans Devanagari for authentic Hindi & Indian scripts
 */

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val PoppinsFont = GoogleFont("Poppins")
val NotoSansDevanagariFont = GoogleFont("Noto Sans Devanagari")

val PoppinsFontFamily = FontFamily(
    Font(googleFont = PoppinsFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = PoppinsFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = PoppinsFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PoppinsFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

val NotoDevanagariFontFamily = FontFamily(
    Font(googleFont = NotoSansDevanagariFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = NotoSansDevanagariFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = NotoSansDevanagariFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = NotoSansDevanagariFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Primary Display & Body Typography
val KaarigarFontFamily = PoppinsFontFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    displaySmall = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = KaarigarFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
