package com.ego.mobile.data.local.mapper

import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitMapperTest {

    @Test
    fun toEntityMapsAllFieldsCorrectly() {
        val habit = Habit(
            id = 42L,
            name = "Leitura diária",
            description = "Ler 10 páginas",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ACTIVE,
            createdAt = 1700000000L,
            updatedAt = 1700001000L
        )

        val entity = habit.toEntity()

        assertEquals(42L, entity.id)
        assertEquals("Leitura diária", entity.name)
        assertEquals("Ler 10 páginas", entity.description)
        assertEquals("DAILY", entity.frequency)
        assertEquals("ACTIVE", entity.status)
        assertEquals(1700000000L, entity.createdAt)
        assertEquals(1700001000L, entity.updatedAt)
    }

    @Test
    fun toDomainMapsAllFieldsCorrectly() {
        val entity = HabitEntity(
            id = 7L,
            name = "Beber água",
            description = null,
            frequency = "DAILY",
            status = "PAUSED",
            createdAt = 1700002000L,
            updatedAt = 1700003000L
        )

        val domain = entity.toDomain()

        assertEquals(7L, domain.id)
        assertEquals("Beber água", domain.name)
        assertNull(domain.description)
        assertEquals(HabitFrequency.Daily, domain.frequency)
        assertEquals(HabitStatus.PAUSED, domain.status)
        assertEquals(1700002000L, domain.createdAt)
        assertEquals(1700003000L, domain.updatedAt)
    }

    @Test
    fun toDomainHandlesUnknownStatusGracefullyAsActive() {
        val entity = HabitEntity(
            id = 1L,
            name = "Meditar",
            description = null,
            frequency = "WEEKLY",
            status = "UNKNOWN_STATUS",
            createdAt = 1700004000L,
            updatedAt = 1700005000L
        )

        val domain = entity.toDomain()

        assertEquals(HabitStatus.ACTIVE, domain.status)
    }
}
