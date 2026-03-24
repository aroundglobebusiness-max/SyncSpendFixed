package com.example.syncspend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SyncSpendTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 34.sp, letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 17.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, color = SecondaryText),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp)
)
