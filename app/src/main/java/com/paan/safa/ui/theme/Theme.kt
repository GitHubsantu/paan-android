package com.paan.safa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PaanColorScheme = lightColorScheme(
    primary = PaanColors.ForestGreen,
    onPrimary = PaanColors.Cream,
    primaryContainer = PaanColors.DeepForest,
    onPrimaryContainer = PaanColors.LightGold,
    secondary = PaanColors.RoyalGold,
    onSecondary = PaanColors.DarkBrown,
    secondaryContainer = PaanColors.LightGold,
    onSecondaryContainer = PaanColors.DarkBrown,
    tertiary = PaanColors.Maroon,
    onTertiary = PaanColors.Cream,
    tertiaryContainer = PaanColors.DeepMaroon,
    onTertiaryContainer = PaanColors.Cream,
    background = PaanColors.Cream,
    onBackground = PaanColors.DarkBrown,
    surface = PaanColors.Cream,
    onSurface = PaanColors.DarkBrown,
    surfaceVariant = PaanColors.CreamSoft,
    onSurfaceVariant = PaanColors.MutedBrown,
    surfaceContainerLowest = PaanColors.CardWhite,
    surfaceContainerLow = PaanColors.CreamSoft,
    outline = PaanColors.MutedBrown,
    outlineVariant = PaanColors.LightGold,
    error = PaanColors.Error,
)

// Light theme only: the Stitch design has no dark variant.
@Composable
fun SafaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PaanColorScheme,
        typography = Typography,
        shapes = PaanShapes,
        content = content,
    )
}