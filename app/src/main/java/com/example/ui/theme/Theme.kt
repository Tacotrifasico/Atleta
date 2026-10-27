package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AthleticDarkColorScheme = darkColorScheme(
    primary = IosOrange,
    onPrimary = Color.White,
    primaryContainer = IosOrangeSubtle,
    onPrimaryContainer = Color(0xFFFFCCBC),
    secondary = IosCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF003640),
    onSecondaryContainer = Color(0xFF80F2FF),
    tertiary = IosGreen,
    onTertiary = Color.White,
    background = IosBackground,
    onBackground = IosTextPrimary,
    surface = IosCard,
    onSurface = IosTextPrimary,
    surfaceVariant = IosCardElevated,
    onSurfaceVariant = IosTextSecondary,
    outline = IosBorder,
    error = IosRed
)

private val AthleticLightColorScheme = lightColorScheme(
    primary = IosOrange,
    onPrimary = Color.White,
    secondary = IosCyan,
    onSecondary = Color.White,
    tertiary = IosGreen,
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF000000),
    surface = Color.White,
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF6C6C70),
    outline = Color(0xFFD1D1D6)
)

@Composable
fun AthleticOSTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AthleticDarkColorScheme else AthleticLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
