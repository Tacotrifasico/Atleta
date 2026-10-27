package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Modern iOS 18 Dark System Palette with Athletic Accent
val IosBackground = Color(0xFF000000)      // Pure OLED black
val IosCard = Color(0xFF14171F)            // Inset grouped surface
val IosCardElevated = Color(0xFF1E222D)    // Secondary elevated surface
val IosBorder = Color(0xFF2B303E)          // Subtle card border
val IosDivider = Color(0xFF222632)

// Apple Human Interface / Athletic System Colors
val IosOrange = Color(0xFFFF5722)          // Athletic Coral Orange
val IosOrangeSubtle = Color(0x33FF5722)
val IosCyan = Color(0xFF0A84FF)            // iOS System Blue / Speed Cyan
val IosGreen = Color(0xFF30D158)           // iOS System Green (Checked / Optimal)
val IosGreenSubtle = Color(0x2630D158)
val IosAmber = Color(0xFFFF9F0A)           // iOS System Amber (Warning)
val IosRed = Color(0xFFFF453A)             // iOS System Red (Danger)

// High-legibility Typography Colors
val IosTextPrimary = Color(0xFFFFFFFF)     // Crisp 100% white
val IosTextSecondary = Color(0xFF98989F)   // High-contrast secondary gray
val IosTextMuted = Color(0xFF636366)       // Tertiary gray

// Theme Aliases
val ObsidianDark = IosBackground
val SurfaceCard = IosCard
val SurfaceElevated = IosCardElevated
val BorderSubtle = IosBorder
val AthleticOrange = IosOrange
val CyberCyan = IosCyan
val RecoveryGreen = IosGreen
val WarningAmber = IosAmber
val DangerRed = IosRed
val TextPrimary = IosTextPrimary
val TextSecondary = IosTextSecondary
val TextMuted = IosTextMuted
