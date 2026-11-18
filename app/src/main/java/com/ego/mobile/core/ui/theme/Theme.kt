package com.ego.mobile.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Mapeamento dos tokens monocromáticos escuros para o ColorScheme do Material 3.
 * Conforme especificado em docs/specs/fase-9-ui-ux-identidade-visual/spec.md (Seção 6.1).
 */
fun egoDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = EgoColors.primary,
    onPrimary = EgoColors.onPrimary,
    primaryContainer = EgoColors.primaryContainer,
    onPrimaryContainer = EgoColors.onPrimaryContainer,
    inversePrimary = EgoColors.primaryContainer,
    secondary = EgoColors.onSurfaceVariant,
    onSecondary = EgoColors.surface,
    secondaryContainer = EgoColors.surfaceVariant,
    onSecondaryContainer = EgoColors.onSurface,
    tertiary = EgoColors.onSurfaceVariant,
    onTertiary = EgoColors.surface,
    tertiaryContainer = EgoColors.primaryContainer,
    onTertiaryContainer = EgoColors.onPrimaryContainer,
    background = EgoColors.background,
    onBackground = EgoColors.onBackground,
    surface = EgoColors.surface,
    onSurface = EgoColors.onSurface,
    surfaceVariant = EgoColors.surfaceVariant,
    onSurfaceVariant = EgoColors.onSurfaceVariant,
    surfaceContainer = EgoColors.surfaceContainer,
    surfaceContainerLowest = EgoColors.background,
    surfaceContainerLow = EgoColors.surface,
    surfaceContainerHigh = EgoColors.surfaceVariant,
    surfaceContainerHighest = EgoColors.surfaceVariant,
    surfaceDim = EgoColors.surface,
    surfaceBright = EgoColors.primaryContainer,
    inverseSurface = EgoColors.onSurface,
    inverseOnSurface = EgoColors.surface,
    outline = EgoColors.outline,
    outlineVariant = EgoColors.surfaceVariant,
    error = EgoColors.error,
    onError = EgoColors.onPrimary,
    errorContainer = EgoColors.errorContainer,
    onErrorContainer = EgoColors.onErrorContainer,
)

@Composable
fun EgoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = egoDarkColorScheme(),
        shapes = EgoShapes,
        typography = EgoTypography,
        content = content
    )
}
