package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class FocusGlassTokens(
    val isDark: Boolean,
    val glassFill: Color,
    val glassBorder: Color,
    val elevatedSurface: Color,
    val secondaryText: Color
)

val LocalFocusGlassTokens = staticCompositionLocalOf {
    FocusGlassTokens(
        isDark = true,
        glassFill = GlassFillDark,
        glassBorder = GlassBorderDark,
        elevatedSurface = SurfaceElevatedDark,
        secondaryText = TextSecondaryDark
    )
}

private val FocusDarkColorScheme = darkColorScheme(
    primary = VioletAccent,
    onPrimary = Color.White,
    secondary = CyanAccent,
    onSecondary = Color(0xFF07131A),
    tertiary = MintSuccess,
    onTertiary = Color(0xFF061A12),
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = GlassBorderDark,
    error = CoralAlert,
    onError = Color.White
)

private val FocusLightColorScheme = lightColorScheme(
    primary = VioletAccent,
    onPrimary = Color.White,
    secondary = CyanAccent,
    onSecondary = Color(0xFF07131A),
    tertiary = MintSuccess,
    onTertiary = Color(0xFF061A12),
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = GlassBorderLight,
    error = CoralAlert,
    onError = Color.White
)

val FocusShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FocusReelsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FocusDarkColorScheme else FocusLightColorScheme
    val glassTokens = if (darkTheme) {
        FocusGlassTokens(
            isDark = true,
            glassFill = GlassFillDark,
            glassBorder = GlassBorderDark,
            elevatedSurface = SurfaceElevatedDark,
            secondaryText = TextSecondaryDark
        )
    } else {
        FocusGlassTokens(
            isDark = false,
            glassFill = GlassFillLight,
            glassBorder = GlassBorderLight,
            elevatedSurface = SurfaceElevatedLight,
            secondaryText = TextSecondaryLight
        )
    }

    CompositionLocalProvider(LocalFocusGlassTokens provides glassTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = FocusShapes,
            content = content
        )
    }
}
