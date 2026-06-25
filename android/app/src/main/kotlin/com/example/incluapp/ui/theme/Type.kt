package com.example.incluapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LexiTypography = Typography(
    displayLarge   = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp,  color = AccentWhite),
    displayMedium  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 26.sp, lineHeight = 34.sp,  color = AccentWhite),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 22.sp, lineHeight = 30.sp,  color = AccentWhite),
    titleLarge     = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 20.sp, lineHeight = 28.sp,  color = AccentWhite),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 16.sp, lineHeight = 24.sp,  color = AccentWhite),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 26.sp,  color = OnSurface),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 22.sp,  color = OnSurface),
    labelLarge     = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, lineHeight = 20.sp,  color = AccentWhite),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 12.sp, lineHeight = 16.sp,  color = DisabledGray),
)
