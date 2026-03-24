package com.example.syncspend.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    background = BackgroundGrey,
    surface = CardWhite,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    secondary = SecondaryText,
    error = DestructiveRed
)

@Composable
fun SyncSpendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = SyncSpendTypography,
        content = content
    )
}
