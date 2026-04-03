package com.example.timetable.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7F0FF),
    onPrimaryContainer = Color(0xFF12284A),
    secondary = SkySecondary,
    onSecondary = Color(0xFF052431),
    secondaryContainer = Color(0xFFE8FBFF),
    onSecondaryContainer = Color(0xFF11485A),
    tertiary = CyanAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1EAFF),
    onTertiaryContainer = Color(0xFF3F2475),
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF5E728C),
    outline = OutlineLight,
    outlineVariant = Color(0xFFE6ECF7),
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DB2FF),
    onPrimary = Color(0xFF08111D),
    primaryContainer = Color(0xFF132542),
    onPrimaryContainer = Color(0xFFE2ECFF),
    secondary = Color(0xFF67E8F9),
    onSecondary = Color(0xFF04141A),
    secondaryContainer = Color(0xFF0E2A33),
    onSecondaryContainer = Color(0xFFD6FBFF),
    tertiary = Color(0xFFC4B5FD),
    onTertiary = Color(0xFF160D30),
    tertiaryContainer = Color(0xFF27184F),
    onTertiaryContainer = Color(0xFFECE7FF),
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = OutlineDark,
    outlineVariant = Color(0xFF1D2A41),
    error = ErrorRed,
    onError = Color.White
)

// ── Minimalist / Nothing Phone-style B&W schemes ─────────────────────────

private val MinimalistLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5E5), // Soft gray for light mode chips
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF52525B), // Zinc 600
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4F4F5), // Zinc 100
    onSecondaryContainer = Color(0xFF18181B),
    tertiary = Color(0xFF71717A), // Zinc 500
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4F4F5),
    onTertiaryContainer = Color(0xFF27272A),
    background = Color(0xFFFAFAFA), // Off-white app background
    onBackground = Color(0xFF09090B), // Near-black text
    surface = Color.White, // Pure white cards inside off-white bg
    onSurface = Color(0xFF09090B),
    surfaceVariant = Color(0xFFF4F4F5), // Zinc 100 for secondary cards
    onSurfaceVariant = Color(0xFF52525B), // Zinc 600 for secondary text
    outline = Color(0xFFE4E4E7), // Zinc 200 borders
    outlineVariant = Color(0xFFF4F4F5),
    error = Color(0xFF333333),
    onError = Color.White
)

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF27272A), // Zinc 800 for dark mode chips
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFA1A1AA), // Zinc 400
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF18181B), // Zinc 900
    onSecondaryContainer = Color(0xFFE4E4E7),
    tertiary = Color(0xFF71717A), // Zinc 500
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF18181B),
    onTertiaryContainer = Color(0xFFE4E4E7),
    background = Color.Black, // Pure black app background
    onBackground = Color.White,
    surface = Color(0xFF121212), // Very dark gray cards
    onSurface = Color(0xFFFAFAFA), // Near-white text
    surfaceVariant = Color(0xFF18181B), // Zinc 900 for secondary cards
    onSurfaceVariant = Color(0xFFA1A1AA), // Zinc 400 for secondary text
    outline = Color(0xFF27272A), // Zinc 800 borders
    outlineVariant = Color(0xFF18181B),
    error = Color(0xFFCCCCCC),
    onError = Color.Black
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

private val CompactShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
)

@Composable
fun TimetableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: Long? = null,
    isMinimalist: Boolean = false,
    compactMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseScheme = when {
        isMinimalist && darkTheme -> MinimalistDarkColorScheme
        isMinimalist -> MinimalistLightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val colorScheme = if (isMinimalist) baseScheme else baseScheme.withAccent(themeColor, darkTheme)

    val navBarColor = colorScheme.background

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = navBarColor.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = if (compactMode || isMinimalist) CompactShapes else AppShapes,
        content = content
    )
}

private fun androidx.compose.material3.ColorScheme.withAccent(
    themeColor: Long?,
    darkTheme: Boolean
): androidx.compose.material3.ColorScheme {
    val accent = themeColor?.let { Color(it.toInt()) } ?: return this
    val onAccent = if (accent.luminance() > 0.55f) Color.Black else Color.White
    val primaryContainer = if (darkTheme) lerp(accent, Color.Black, 0.72f) else lerp(accent, Color.White, 0.82f)
    val secondaryContainer = if (darkTheme) lerp(accent, Color.Black, 0.84f) else lerp(accent, Color.White, 0.9f)
    val tertiaryContainer = if (darkTheme) lerp(accent, Color.Black, 0.62f) else lerp(accent, Color.White, 0.74f)
    return copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = primaryContainer,
        onPrimaryContainer = if (primaryContainer.luminance() > 0.55f) Color.Black else Color.White,
        secondary = lerp(accent, secondary, 0.35f),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = if (secondaryContainer.luminance() > 0.55f) Color.Black else Color.White,
        tertiary = lerp(accent, tertiary, 0.25f),
        onTertiary = onAccent,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = if (tertiaryContainer.luminance() > 0.55f) Color.Black else Color.White
    )
}
