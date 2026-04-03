package com.example.timetable.ui.theme

import androidx.compose.ui.graphics.Color

// Core brand palette
val BluePrimary = Color(0xFF3B82F6)
val SkySecondary = Color(0xFF22D3EE)
val CyanAccent = Color(0xFF7C3AED)

// UI overhaul colors
val DarkHeader = Color(0xFFF7FAFF)
val DarkHeaderStart = Color(0xFFFDFEFF)
val DarkHeaderMid = Color(0xFFF1F6FF)
val DarkHeaderEnd = Color(0xFFE7F0FF)
val SoftBackground = Color(0xFFF3F7FF)
val MistBackground = Color(0xFFF7F9FE)
val UpNextGradientStart = Color(0xFF2563EB)
val UpNextGradientEnd = Color(0xFF06B6D4)
val VibrantPink = Color(0xFFE879F9)
val VibrantOrange = Color(0xFFF59E0B)
val VibrantCyan = Color(0xFF67E8F9)
val SoftRedBackground = Color(0xFFFDE8EA)
val RedText = Color(0xFFDC2626)
val WarningBg = Color(0xFFFFF1CC)
val WarningText = Color(0xFFB45309)
val SafeBg = Color(0xFFDCFCE7)
val SafeText = Color(0xFF15803D)

// Light Theme
val BackgroundLight = MistBackground
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF2F5FB)
val OnBackgroundLight = Color(0xFF0F172A)
val OnSurfaceLight = Color(0xFF0F172A)
val OutlineLight = Color(0xFFD8E1F0)

// Dark Theme
val BackgroundDark = Color(0xFF07111F)
val SurfaceDark = Color(0xFF0D1728)
val SurfaceVariantDark = Color(0xFF132036)
val OnBackgroundDark = Color(0xFFF4F7FB)
val OnSurfaceDark = Color(0xFFF4F7FB)
val OutlineDark = Color(0xFF24324A)

// Functional
val SuccessGreen = Color(0xFF10B981)  // Emerald 500
val WarningYellow = Color(0xFFF59E0B) // Amber 500
val ErrorRed = Color(0xFFEF4444)      // Red 500
val InfoBlue = Color(0xFF3B82F6)      // Blue 500

// Gradients
val GradientPrimary = listOf(BluePrimary, SkySecondary)
val GradientUpNext = listOf(UpNextGradientStart, UpNextGradientEnd)
val GradientHeader = listOf(DarkHeaderStart, DarkHeaderMid, DarkHeaderEnd)

val DarkHeaderStartNight = Color(0xFF081220)
val DarkHeaderMidNight = Color(0xFF0D1A2E)
val DarkHeaderEndNight = Color(0xFF101B30)
val GradientHeaderDark = listOf(DarkHeaderStartNight, DarkHeaderMidNight, DarkHeaderEndNight)

val GradientHeroCard = listOf(BluePrimary, CyanAccent, SkySecondary)
val GradientAccentWarm = listOf(VibrantOrange, VibrantPink)
val GradientAccentCool = listOf(SkySecondary, VibrantCyan)
val GradientMeshLight = listOf(Color(0xFFEAF1FF), Color(0xFFF8FBFF), Color(0xFFEFFBFF))
val GradientMeshDark = listOf(Color(0xFF081220), Color(0xFF0C1830), Color(0xFF0A1427))
