package com.ego.mobile.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.ego.mobile.core.ui.theme.EgoColors
import com.ego.mobile.core.ui.theme.EgoShape
import com.ego.mobile.core.ui.theme.EgoSpacing
import com.ego.mobile.core.ui.theme.egoDarkColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class DarkThemeContrastTest {
    private val colors = egoDarkColorScheme()

    @Test
    fun colorSchemeMatchesApprovedTokens() {
        assertEquals("background", EgoColors.background, colors.background)
        assertEquals("surface", EgoColors.surface, colors.surface)
        assertEquals("surfaceContainer", EgoColors.surfaceContainer, colors.surfaceContainer)
        assertEquals("surfaceVariant", EgoColors.surfaceVariant, colors.surfaceVariant)
        assertEquals("outline", EgoColors.outline, colors.outline)
        assertEquals("onBackground", EgoColors.onBackground, colors.onBackground)
        assertEquals("onSurface", EgoColors.onSurface, colors.onSurface)
        assertEquals("onSurfaceVariant", EgoColors.onSurfaceVariant, colors.onSurfaceVariant)
        assertEquals("primary", EgoColors.primary, colors.primary)
        assertEquals("onPrimary", EgoColors.onPrimary, colors.onPrimary)
        assertEquals("primaryContainer", EgoColors.primaryContainer, colors.primaryContainer)
        assertEquals("onPrimaryContainer", EgoColors.onPrimaryContainer, colors.onPrimaryContainer)
        assertEquals("error", EgoColors.error, colors.error)
        assertEquals("errorContainer", EgoColors.errorContainer, colors.errorContainer)
        assertEquals("onErrorContainer", EgoColors.onErrorContainer, colors.onErrorContainer)
    }

    @Test
    fun textAndStatusColorsMeetWcagAaContrast() {
        assertContrast("onBackground/background", colors.onBackground, colors.background)
        assertContrast("onSurface/surface", colors.onSurface, colors.surface)
        assertContrast("onSurface/surfaceContainer", colors.onSurface, colors.surfaceContainer)
        assertContrast("onSurfaceVariant/surfaceVariant", colors.onSurfaceVariant, colors.surfaceVariant)
        assertContrast("onSurfaceVariant/surface", colors.onSurfaceVariant, colors.surface)
        assertContrast("onSurfaceVariant/background", colors.onSurfaceVariant, colors.background)
        assertContrast("primary/background", colors.primary, colors.background)
        assertContrast("onPrimary/primary", colors.onPrimary, colors.primary)
        assertContrast("onPrimaryContainer/primaryContainer", colors.onPrimaryContainer, colors.primaryContainer)
        assertContrast("onErrorContainer/errorContainer", colors.onErrorContainer, colors.errorContainer)
        assertContrast("error/background", colors.error, colors.background)
    }

    @Test
    fun shapeAndSpacingTokensMatchApprovedSpec() {
        assertEquals(12.dp, EgoShape.control)
        assertEquals(16.dp, EgoShape.surface)
        assertEquals(999.dp, EgoShape.pill)

        assertEquals(4.dp, EgoSpacing.xs)
        assertEquals(8.dp, EgoSpacing.sm)
        assertEquals(12.dp, EgoSpacing.md)
        assertEquals(16.dp, EgoSpacing.lg)
        assertEquals(20.dp, EgoSpacing.xl)
        assertEquals(24.dp, EgoSpacing.xl2)
        assertEquals(32.dp, EgoSpacing.xxxl)
    }

    private fun assertContrast(label: String, foreground: Color, background: Color) {
        val foregroundLuminance = foreground.luminance()
        val backgroundLuminance = background.luminance()
        val ratio = (max(foregroundLuminance, backgroundLuminance) + 0.05f) /
            (min(foregroundLuminance, backgroundLuminance) + 0.05f)

        assertTrue("$label contrast ratio $ratio is below WCAG AA 4.5:1", ratio >= 4.5f)
    }
}
