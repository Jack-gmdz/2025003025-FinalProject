package com.example.text.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Blue90,
    secondary = Blue30,
    surface = androidx.compose.ui.graphics.Color.White,
    background = GreyLight,
    error = RedPriority
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = DarkBlue10,
    primaryContainer = Blue30,
    secondary = Blue90,
    surface = GreyDark,
    background = DarkBlue10,
    error = RedPriority
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
