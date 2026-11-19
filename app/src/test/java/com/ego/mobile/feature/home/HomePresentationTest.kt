package com.ego.mobile.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomePresentationTest {
    @Test fun `greeting follows Sao Paulo day periods`() {
        assertEquals("Boa noite", greetingForHour(4))
        assertEquals("Bom dia", greetingForHour(5))
        assertEquals("Bom dia", greetingForHour(11))
        assertEquals("Boa tarde", greetingForHour(12))
        assertEquals("Boa tarde", greetingForHour(17))
        assertEquals("Boa noite", greetingForHour(18))
    }
}
