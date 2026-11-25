package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Focus Reels Palette — Calm, modern, zero gold/yellow/brown tones
val ObsidianBg = Color(0xFF0A0A0F)
val SurfaceDark = Color(0xFF14141C)
val SurfaceElevatedDark = Color(0xFF1C1C28)

val LightBg = Color(0xFFF5F6FA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFECEEF6)

val VioletAccent = Color(0xFF7C5CFF)
val CyanAccent = Color(0xFF22D3EE)
val MintSuccess = Color(0xFF34D399)
val CoralAlert = Color(0xFFFF5E7E)

val TextPrimaryDark = Color(0xFFF5F5FA)
val TextSecondaryDark = Color(0xFF9A9AB0)

val TextPrimaryLight = Color(0xFF11111A)
val TextSecondaryLight = Color(0xFF5C5C74)

val GlassFillDark = Color.White.copy(alpha = 0.06f)
val GlassBorderDark = Color.White.copy(alpha = 0.08f)

val GlassFillLight = Color(0xFF0A0A0F).copy(alpha = 0.04f)
val GlassBorderLight = Color(0xFF0A0A0F).copy(alpha = 0.08f)

val PrimaryAccentGradient = Brush.linearGradient(
    colors = listOf(VioletAccent, CyanAccent)
)

val HorizontalAccentGradient = Brush.horizontalGradient(
    colors = listOf(VioletAccent, CyanAccent)
)

val VerticalAccentGradient = Brush.verticalGradient(
    colors = listOf(CyanAccent, VioletAccent)
)

// Distinct atmospheric gradient palettes for video placeholders (zero gold/yellow/brown)
val ReelGradients: List<List<Color>> = listOf(
    listOf(Color(0xFF191233), Color(0xFF281C59), Color(0xFF0C2E45)),
    listOf(Color(0xFF0E1D2F), Color(0xFF143C54), Color(0xFF165258)),
    listOf(Color(0xFF1C1338), Color(0xFF3E2278), Color(0xFF1E3A6E)),
    listOf(Color(0xFF0C242B), Color(0xFF134848), Color(0xFF1E3A5F)),
    listOf(Color(0xFF17132E), Color(0xFF2A245E), Color(0xFF124E66)),
    listOf(Color(0xFF0D1E36), Color(0xFF1B3A6B), Color(0xFF1E5B70)),
    listOf(Color(0xFF16182E), Color(0xFF2F2566), Color(0xFF144B5C)),
    listOf(Color(0xFF11232E), Color(0xFF1D4D54), Color(0xFF2E2A6E))
)
