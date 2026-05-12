package ru.gr05307.kareta05307.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Custom Color Schemes for Карета 05-307
val CustomLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF1976D2),        // Rich Blue
    onPrimary = Color(0xFFFFFFFF),       // White
    primaryContainer = Color(0xFFE3F2FD), // Light Blue
    onPrimaryContainer = Color(0xFF0D47A1), // Dark Blue

    secondary = Color(0xFF9C27B0),       // Purple
    onSecondary = Color(0xFFFFFFFF),      // White
    secondaryContainer = Color(0xFFF3E5F5), // Light Purple
    onSecondaryContainer = Color(0xFF4A148C), // Dark Purple

    tertiary = Color(0xFF009688),         // Teal
    onTertiary = Color(0xFFFFFFFF),       // White
    tertiaryContainer = Color(0xFFE0F2F1), // Light Teal
    onTertiaryContainer = Color(0xFF004D40), // Dark Teal

    background = Color(0xFFF5F5F5),       // Light Gray
    onBackground = Color(0xFF212121),     // Almost Black
    surface = Color(0xFFFFFFFF),          // White
    onSurface = Color(0xFF212121),        // Almost Black

    surfaceVariant = Color(0xFFE8EAF6),   // Very Light Indigo
    onSurfaceVariant = Color(0xFF5C5C5C), // Medium Gray

    error = Color(0xFFD32F2F),            // Red
    onError = Color(0xFFFFFFFF),          // White
    errorContainer = Color(0xFFFFEBEE),   // Light Red
    onErrorContainer = Color(0xFFB71C1C), // Dark Red

    outline = Color(0xFFBDBDBD),          // Light Gray
    outlineVariant = Color(0xFFE0E0E0),   // Very Light Gray

    inverseSurface = Color(0xFF121212),   // Dark Gray
    inverseOnSurface = Color(0xFFFFFFFF), // White
    inversePrimary = Color(0xFF90CAF9),   // Light Blue
)

val CustomDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF90CAF9),         // Light Blue
    onPrimary = Color(0xFF0D47A1),        // Dark Blue
    primaryContainer = Color(0xFF1565C0), // Medium Blue
    onPrimaryContainer = Color(0xFFE3F2FD), // Light Blue

    secondary = Color(0xFFCE93D8),        // Light Purple
    onSecondary = Color(0xFF4A148C),      // Dark Purple
    secondaryContainer = Color(0xFF6A1B9A), // Medium Purple
    onSecondaryContainer = Color(0xFFF3E5F5), // Light Purple

    tertiary = Color(0xFF80CBC4),         // Light Teal
    onTertiary = Color(0xFF004D40),       // Dark Teal
    tertiaryContainer = Color(0xFF00695C), // Medium Teal
    onTertiaryContainer = Color(0xFFE0F2F1), // Light Teal

    background = Color(0xFF121212),       // Dark Gray
    onBackground = Color(0xFFE0E0E0),     // Light Gray
    surface = Color(0xFF1E1E1E),          // Slightly lighter dark
    onSurface = Color(0xFFE0E0E0),        // Light Gray

    surfaceVariant = Color(0xFF2C2C2C),   // Darker surface
    onSurfaceVariant = Color(0xFFBDBDBD), // Light Gray

    error = Color(0xFFEF5350),            // Light Red
    onError = Color(0xFF212121),          // Dark Gray
    errorContainer = Color(0xFFC62828),   // Dark Red
    onErrorContainer = Color(0xFFFFEBEE), // Light Red

    outline = Color(0xFF424242),          // Dark Gray
    outlineVariant = Color(0xFF2C2C2C),   // Very Dark Gray

    inverseSurface = Color(0xFFE0E0E0),   // Light Gray
    inverseOnSurface = Color(0xFF121212), // Dark Gray
    inversePrimary = Color(0xFF1976D2),   // Rich Blue
)
