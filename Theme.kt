package com.vampire.browser.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = VampireCrimson,
    onPrimary = VampireBlack,
    surface = VampireSurface,
    onSurface = VampireTextPrimary,
    background = VampireBlack,
    onBackground = VampireTextPrimary,
    surfaceVariant = VampireCard,
    outline = VampireBorder
)

@Composable
fun VampireTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}