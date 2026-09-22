package com.kartview.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0B3050),
    secondary = Color(0xFFB7C9F0),
    onSecondary = Color(0xFF1B2840),
    background = Color(0xFF0F1118),
    onBackground = Color(0xFFE6E9F0),
    surface = Color(0xFF1A1D28),
    onSurface = Color(0xFFE6E9F0),
    surfaceVariant = Color(0xFF262B38),
    onSurfaceVariant = Color(0xFFB9C0D4),
    outline = Color(0xFF8A92A8),
    outlineVariant = Color(0xFF3A4152),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6B7EA8),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF2F5FA),
    onBackground = Color(0xFF11131A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF11131A),
    surfaceVariant = Color(0xFFE4E9F2),
    onSurfaceVariant = Color(0xFF454B56),
    outline = Color(0xFF7A8291),
    outlineVariant = Color(0xFFC7CFDD),
)

@Composable
fun KartviewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}