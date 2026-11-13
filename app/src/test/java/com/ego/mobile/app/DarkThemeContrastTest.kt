package com.ego.mobile.app

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class DarkThemeContrastTest {
    private val colors = darkColorScheme()

    @Test
    fun textAndStatusColorsMeetWcagAaContrast() {
        assertContrast("onBackground/background", colors.onBackground, colors.background)
        assertContrast("onSurface/surface", colors.onSurface, colors.surface)
        assertContrast("onSurfaceVariant/surfaceVariant", colors.onSurfaceVariant, colors.surfaceVariant)
        assertContrast("onSurfaceVariant/background", colors.onSurfaceVariant, colors.background)
        assertContrast("primary/background", colors.primary, colors.background)
        assertContrast("onPrimary/primary", colors.onPrimary, colors.primary)
        assertContrast("onPrimaryContainer/primaryContainer", colors.onPrimaryContainer, colors.primaryContainer)
        assertContrast("onSecondaryContainer/secondaryContainer", colors.onSecondaryContainer, colors.secondaryContainer)
        assertContrast("onTertiaryContainer/tertiaryContainer", colors.onTertiaryContainer, colors.tertiaryContainer)
        assertContrast("onErrorContainer/errorContainer", colors.onErrorContainer, colors.errorContainer)
        assertContrast("error/background", colors.error, colors.background)
    }

    private fun assertContrast(label: String, foreground: Color, background: Color) {
        val foregroundLuminance = foreground.luminance()
        val backgroundLuminance = background.luminance()
        val ratio = (max(foregroundLuminance, backgroundLuminance) + 0.05f) /
            (min(foregroundLuminance, backgroundLuminance) + 0.05f)

        assertTrue("$label contrast ratio $ratio is below WCAG AA 4.5:1", ratio >= 4.5f)
    }
}
