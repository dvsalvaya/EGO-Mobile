package com.ego.mobile.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Design tokens de formas e raios para a Fase 9.
 * Conforme especificado em docs/specs/fase-9-ui-ux-identidade-visual/spec.md (Seção 6.3).
 */
object EgoShape {
    val control = 12.dp // controles e elementos compactos
    val surface = 16.dp // superfícies e Bottom Sheets
    val pill = 999.dp   // pills reservadas a chips e badges
}

val EgoShapes = Shapes(
    small = RoundedCornerShape(EgoShape.control),
    medium = RoundedCornerShape(EgoShape.control),
    large = RoundedCornerShape(EgoShape.surface),
    extraLarge = RoundedCornerShape(EgoShape.surface),
)
