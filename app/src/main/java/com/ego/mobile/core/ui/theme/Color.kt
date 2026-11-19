package com.ego.mobile.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens de cores para a Fase 9 — Monocromático Escuro.
 * Conforme especificado em docs/specs/fase-9-ui-ux-identidade-visual/spec.md (Seção 6.1).
 */
object EgoColors {
    // Paleta principal monocromática
    val background = Color(0xFF0B0B0C)
    val surface = Color(0xFF141416)
    val surfaceContainer = Color(0xFF1D1D20)
    val surfaceVariant = Color(0xFF29292D)
    val outline = Color(0xFF414146)
    val onBackground = Color(0xFFF5F5F5)
    val onSurface = Color(0xFFF5F5F5)
    val onSurfaceVariant = Color(0xFFB9B9BF)
    val primary = Color(0xFFF2F2F2)
    val onPrimary = Color(0xFF111111)
    val primaryContainer = Color(0xFF333337)
    val onPrimaryContainer = Color(0xFFF5F5F5)

    // Cores semânticas de erro e atraso
    val error = Color(0xFFFF8A80)
    val errorContainer = Color(0xFF5A1B1B)
    val onErrorContainer = Color(0xFFFFDAD6)

    // Cores semânticas para a grade de consistência/hábitos (Seção 8.1)
    object HabitGrid {
        val noForecast = Color(0xFF29292D)
        val zeroPercent = Color(0xFFFF8A80)
        val lowProgress = Color(0xFFFFB74D) // > 0% e < 50%
        val mediumProgress = Color(0xFF43A047) // >= 50% e < 100%
        val fullProgress = Color(0xFF81C784) // 100%
    }
}
