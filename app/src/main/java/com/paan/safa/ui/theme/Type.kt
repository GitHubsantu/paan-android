package com.paan.safa.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.paan.safa.R

val Epilogue = FontFamily(
    Font(R.font.epilogue_semibold, FontWeight.SemiBold),
    Font(R.font.epilogue_bold, FontWeight.Bold),
)

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

private fun style(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    line: Int,
    spacing: TextUnit = 0.sp,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing,
)

val Typography = Typography(
    displayLarge = style(Epilogue, FontWeight.Bold, 40, 48, (-0.02).em),
    displayMedium = style(Epilogue, FontWeight.Bold, 32, 40, (-0.02).em),
    headlineLarge = style(Epilogue, FontWeight.SemiBold, 28, 36, (-0.01).em),
    headlineMedium = style(Epilogue, FontWeight.SemiBold, 22, 28),
    titleLarge = style(Epilogue, FontWeight.SemiBold, 18, 24),
    titleMedium = style(Manrope, FontWeight.SemiBold, 16, 22),
    bodyLarge = style(Manrope, FontWeight.Normal, 16, 24),
    bodyMedium = style(Manrope, FontWeight.Normal, 14, 20),
    bodySmall = style(Manrope, FontWeight.Normal, 12, 16),
    labelLarge = style(Manrope, FontWeight.SemiBold, 14, 20, 0.01.em),
    labelMedium = style(Manrope, FontWeight.SemiBold, 12, 16, 0.02.em),
    labelSmall = style(Manrope, FontWeight.Medium, 11, 14, 0.03.em),
)

/** Styles that don't map onto Material roles. */
object PaanTextStyles {
    val Currency = style(Epilogue, FontWeight.Bold, 20, 24)
    val TokenCode = style(JetBrainsMono, FontWeight.Bold, 24, 28, 0.08.em)
}