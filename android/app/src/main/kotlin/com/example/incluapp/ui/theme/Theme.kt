package com.example.incluapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LexiDarkColorScheme = darkColorScheme(
    primary         = PrimaryYellow,
    onPrimary       = OnPrimaryYellow,
    background      = PrimaryBackground,
    onBackground    = AccentWhite,
    surface         = Surface,
    onSurface       = OnSurface,
    surfaceVariant  = SurfaceVariant,
    secondary       = SuccessGreen,
    error           = ErrorRed,
    outline         = DisabledGray,
)

@Composable
fun LexiEduTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LexiDarkColorScheme,
        typography  = LexiTypography,
        content     = content
    )
}
