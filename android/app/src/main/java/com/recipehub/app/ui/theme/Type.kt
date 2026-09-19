package com.recipehub.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.recipehub.app.R

// Nunito and Figtree ship only as variable fonts from Google Fonts (no static per-weight
// files), so each named weight is pulled from the single variable font via FontVariation.

@OptIn(ExperimentalTextApi::class)
private fun nunitoFont(weight: FontWeight, italic: Boolean = false) = Font(
    resId = if (italic) R.font.nunito_italic else R.font.nunito,
    weight = weight,
    style = if (italic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val NunitoFamily = FontFamily(
    nunitoFont(FontWeight.Bold),
    nunitoFont(FontWeight.ExtraBold),
)

@OptIn(ExperimentalTextApi::class)
private fun figtreeFont(weight: FontWeight) = Font(
    resId = R.font.figtree,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val FigtreeFamily = FontFamily(
    figtreeFont(FontWeight.Normal),
    figtreeFont(FontWeight.Medium),
    figtreeFont(FontWeight.SemiBold),
    figtreeFont(FontWeight.Bold),
)

// Maps the handoff's type-role table onto Material3's Typography slots. Roles the handoff
// didn't specify (display*, headlineLarge/Medium, bodySmall) keep M3's defaults.
val RecipeHubTypography = Typography(
    // Screen title (detail)
    headlineSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 25.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp,
    ),
    // Wordmark / dialog title
    titleLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    // Card title
    titleMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.5.sp,
        lineHeight = 17.5.sp,
    ),
    // List-row title
    titleSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.2.sp,
    ),
    // Body (ingredients, steps)
    bodyLarge = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.5.sp,
        lineHeight = 21.7.sp,
    ),
    // Filter pill / list name
    bodyMedium = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    // Button label
    labelLarge = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    // Section label (apply .uppercase() at the call site — TextStyle has no text-transform)
    labelMedium = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.78.sp,
    ),
    // Meta / count
    labelSmall = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 15.sp,
    ),
)
