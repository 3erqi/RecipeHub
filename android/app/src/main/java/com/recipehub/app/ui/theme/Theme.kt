package com.recipehub.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Vivid grass-green accent (matches the app icon) as the primary color. Light mode
// keeps green-tinted neutrals; dark mode uses a neutral dark grey background/surface
// instead, so the green accent pops against a plain backdrop rather than blending
// into an all-green dark screen.

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F6B29),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6B0),
    onPrimaryContainer = Color(0xFF122B06),
    secondary = Color(0xFFB5471B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBC7),
    onSecondaryContainer = Color(0xFF3A1400),
    tertiary = Color(0xFF8C6A1F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE08C),
    onTertiaryContainer = Color(0xFF2B1E00),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF5FAF0),
    onBackground = Color(0xFF14210F),
    surface = Color(0xFFF5FAF0),
    onSurface = Color(0xFF14210F),
    surfaceVariant = Color(0xFFDCEAD2),
    onSurfaceVariant = Color(0xFF3E4F3A),
    outline = Color(0xFF7A8F74),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BC34A),
    onPrimary = Color(0xFF123402),
    primaryContainer = Color(0xFF2E5C1B),
    onPrimaryContainer = Color(0xFFC8E6B0),
    secondary = Color(0xFFFFB59A),
    onSecondary = Color(0xFF5F1900),
    secondaryContainer = Color(0xFF852F04),
    onSecondaryContainer = Color(0xFFFFDBC7),
    tertiary = Color(0xFFECC26D),
    onTertiary = Color(0xFF473300),
    tertiaryContainer = Color(0xFF664B00),
    onTertiaryContainer = Color(0xFFFFE08C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF17181A),
    onBackground = Color(0xFFECEDEE),
    surface = Color(0xFF17181A),
    onSurface = Color(0xFFECEDEE),
    surfaceVariant = Color(0xFF2B2D30),
    onSurfaceVariant = Color(0xFFC4C7CA),
    outline = Color(0xFF5C7A5A),
)

@Composable
fun RecipeHubTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
