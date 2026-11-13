package com.ego.mobile.domain

import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircarePreset
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.HaircareRoutineSource
import com.ego.mobile.domain.model.HaircareStepType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HaircareDomainTest {

    @Test
    fun `routine name cannot be blank`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            HaircareRoutine(name = "   ")
        }
        assertTrue(ex.message.orEmpty().contains("name", ignoreCase = true))
    }

    @Test
    fun `routine name must not exceed 80 characters`() {
        val longName = "A".repeat(81)
        val ex = assertThrows(IllegalArgumentException::class.java) {
            HaircareRoutine(name = longName)
        }
        assertTrue(ex.message.orEmpty().contains("80", ignoreCase = true))
    }

    @Test
    fun `routine description must not exceed 250 characters`() {
        val longDesc = "D".repeat(251)
        val ex = assertThrows(IllegalArgumentException::class.java) {
            HaircareRoutine(name = "Rotina Válida", description = longDesc)
        }
        assertTrue(ex.message.orEmpty().contains("250", ignoreCase = true))
    }

    @Test
    fun `valid routine creation with default source and timestamps`() {
        val routine = HaircareRoutine(
            name = "Meu Cronograma",
            source = HaircareRoutineSource.CUSTOM,
            description = "Rotina para cachos",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        assertEquals("Meu Cronograma", routine.name)
        assertEquals(HaircareRoutineSource.CUSTOM, routine.source)
        assertEquals("Rotina para cachos", routine.description)
    }

    @Test
    fun `occurrence step title cannot be blank`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            HaircareOccurrence(
                stepType = HaircareStepType.HIDRATACAO,
                stepTitle = "",
                scheduledDate = "2026-11-13",
            )
        }
        assertTrue(ex.message.orEmpty().contains("title", ignoreCase = true))
    }

    @Test
    fun `occurrence scheduledDate must follow ISO YYYY-MM-DD format`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            HaircareOccurrence(
                stepType = HaircareStepType.HIDRATACAO,
                stepTitle = "Hidratação Profunda",
                scheduledDate = "13/11/2026",
            )
        }
        assertTrue(ex.message.orEmpty().contains("YYYY-MM-DD", ignoreCase = true))
    }

    @Test
    fun `complete occurrence updates status, executedAt and notes`() {
        val occurrence = HaircareOccurrence(
            id = 1L,
            routineId = 10L,
            stepType = HaircareStepType.HIDRATACAO,
            stepTitle = "Hidratação com Babosa",
            scheduledDate = "2026-11-13",
            status = HaircareOccurrenceStatus.PENDING,
        )

        val completed = occurrence.complete(timestamp = 1700000000L, executionNotes = "Máscara hidratante usada")
        assertEquals(HaircareOccurrenceStatus.COMPLETED, completed.status)
        assertEquals(1700000000L, completed.executedAt)
        assertEquals("Máscara hidratante usada", completed.notes)
    }

    @Test
    fun `reschedule occurrence updates scheduledDate and sets status to RESCHEDULED`() {
        val occurrence = HaircareOccurrence(
            id = 1L,
            routineId = 10L,
            stepType = HaircareStepType.NUTRICAO,
            stepTitle = "Nutrição com Óleo de Coco",
            scheduledDate = "2026-11-13",
            status = HaircareOccurrenceStatus.PENDING,
        )

        val rescheduled = occurrence.reschedule(newDate = "2026-11-15")
        assertEquals("2026-11-15", rescheduled.scheduledDate)
        assertEquals(HaircareOccurrenceStatus.RESCHEDULED, rescheduled.status)
    }

    @Test
    fun `HaircarePreset standardSchedule provides 12 balanced steps for 4 weeks`() {
        val steps = HaircarePreset.standardSteps()
        assertEquals(12, steps.size)

        // 4 semanas de 3 etapas cada:
        // S1: H, N, H
        // S2: H, N, H
        // S3: H, N, R
        // S4: H, N, H
        val expectedTypes = listOf(
            HaircareStepType.HIDRATACAO, HaircareStepType.NUTRICAO, HaircareStepType.HIDRATACAO,
            HaircareStepType.HIDRATACAO, HaircareStepType.NUTRICAO, HaircareStepType.HIDRATACAO,
            HaircareStepType.HIDRATACAO, HaircareStepType.NUTRICAO, HaircareStepType.RECONSTRUCAO,
            HaircareStepType.HIDRATACAO, HaircareStepType.NUTRICAO, HaircareStepType.HIDRATACAO,
        )
        assertEquals(expectedTypes, steps.map { it.stepType })
    }

    @Test
    fun `HaircarePreset createStandardOccurrences produces valid editable occurrences`() {
        val occurrences = HaircarePreset.createStandardOccurrences(routineId = 5L, startDate = "2026-11-13")
        assertEquals(12, occurrences.size)

        occurrences.forEachIndexed { index, occurrence ->
            assertEquals(5L, occurrence.routineId)
            assertEquals(index, occurrence.orderIndex)
            assertEquals(HaircareOccurrenceStatus.PENDING, occurrence.status)
            assertTrue(occurrence.stepTitle.isNotBlank())
            assertTrue(occurrence.scheduledDate.matches(Regex("""^\d{4}-\d{2}-\d{2}$""")))
        }

        // First occurrence is on start date
        assertEquals("2026-11-13", occurrences.first().scheduledDate)
    }

    @Test
    fun `HaircareStepType contains all approved types and human display names`() {
        val types = HaircareStepType.entries
        assertEquals(5, types.size)
        assertEquals("Hidratação", HaircareStepType.HIDRATACAO.displayName)
        assertEquals("Nutrição", HaircareStepType.NUTRICAO.displayName)
        assertEquals("Reconstrução", HaircareStepType.RECONSTRUCAO.displayName)
        assertEquals("Umectação", HaircareStepType.UMECTACAO.displayName)
        assertEquals("Outro", HaircareStepType.OUTRO.displayName)
    }
}
