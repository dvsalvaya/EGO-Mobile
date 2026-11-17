package com.ego.mobile.domain

import com.ego.mobile.domain.model.WorkoutTemplate
import com.ego.mobile.domain.model.WorkoutTemplateExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WorkoutTemplateDomainTest {

    @Test
    fun `template requires a non blank name`() {
        assertThrows(IllegalArgumentException::class.java) { WorkoutTemplate(name = "   ") }
    }

    @Test
    fun `exercise validation rejects blank names non positive sets and negative weight`() {
        assertThrows(IllegalArgumentException::class.java) {
            exercise(name = "  ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            exercise(name = "Supino", setsCount = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            exercise(name = "Supino", weightKg = -1.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            exercise(name = "Supino", orderIndex = -1)
        }
    }

    @Test
    fun `ordered exercises follow the persisted order index`() {
        val third = exercise(id = 3L, name = "Tríceps", orderIndex = 2)
        val first = exercise(id = 1L, name = "Supino", orderIndex = 0)
        val second = exercise(id = 2L, name = "Agachamento", orderIndex = 1)

        val template = WorkoutTemplate(
            name = "Treino A",
            exercises = listOf(third, first, second),
        )

        assertEquals(listOf(1L, 2L, 3L), template.orderedExercises.map { it.id })
    }

    @Test
    fun `optional textual fields stay untouched`() {
        val exercise = exercise(name = "Supino", reps = "8-10", notes = "cadência lenta", weightKg = 80.0)

        assertEquals("8-10", exercise.reps)
        assertEquals("cadência lenta", exercise.notes)
        assertEquals(80.0, exercise.weightKg!!, 0.01)
    }

    private fun exercise(
        id: Long = 1L,
        name: String,
        orderIndex: Int = 0,
        setsCount: Int = 3,
        weightKg: Double? = null,
        reps: String? = null,
        notes: String? = null,
    ) = WorkoutTemplateExercise(
        id = id,
        name = name,
        orderIndex = orderIndex,
        setsCount = setsCount,
        weightKg = weightKg,
        reps = reps,
        notes = notes,
    )
}
