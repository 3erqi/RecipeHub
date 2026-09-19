package com.recipehub.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// From the Claude Design handoff (design-handoff/README.md). Light mode is the handoff's
// cream/forest/grass palette as-is. Dark mode keeps the neutral dark-grey background chosen
// earlier (not the handoff's forest-green dark variant) but shares the same grass accent, so
// the brand color is consistent across both.

private val Forest = Color(0xFF0A2318)
private val Cream = Color(0xFFF6F2E6)
private val Grass = Color(0xFF5CB944)
private val GrassDeep = Color(0xFF3F6B29)
private val ChipInk = Color(0xFF2C4F1C)
private val Danger = Color(0xFFB5471B)

private val LightColors = lightColorScheme(
    primary = GrassDeep,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6B0),
    onPrimaryContainer = Color(0xFF122B06),
    secondary = Danger,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBC7),
    onSecondaryContainer = Color(0xFF3A1400),
    tertiary = Grass,
    onTertiary = Forest,
    tertiaryContainer = Color(0xFFE2F2DD),
    onTertiaryContainer = ChipInk,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Cream,
    onBackground = Forest,
    surface = Cream,
    onSurface = Forest,
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Forest.copy(alpha = 0.6f),
    outline = Forest.copy(alpha = 0.1f),
)

private val DarkColors = darkColorScheme(
    primary = Grass,
    onPrimary = Forest,
    primaryContainer = Color(0xFF2E5C1B),
    onPrimaryContainer = Color(0xFFC8E6B0),
    secondary = Color(0xFFFFB59A),
    onSecondary = Color(0xFF5F1900),
    secondaryContainer = Color(0xFF852F04),
    onSecondaryContainer = Color(0xFFFFDBC7),
    tertiary = Grass,
    onTertiary = Forest,
    tertiaryContainer = Color(0xFF2E5C1B),
    onTertiaryContainer = Color(0xFFC8E6B0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF17181A),
    onBackground = Color(0xFFECEDEE),
    surface = Color(0xFF17181A),
    onSurface = Color(0xFFECEDEE),
    surfaceVariant = Color(0xFF2B2D30),
    onSurfaceVariant = Color(0xFFC4C7CA),
    outline = GrassDeep,
)

@Composable
fun RecipeHubTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = RecipeHubTypography, content = content)
}
