package com.chirag.mello.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.chirag.mello.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val PoppinsFamily = FontFamily(
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Normal),
)

val NunitoFamily = FontFamily(
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Medium),
)

val MelloTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = TextSecondary
    )
)
