package com.ego.mobile.data.local

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.model.habitZone
import com.ego.mobile.domain.usecase.history
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate

class HabitCalendarPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "habit_calendar_reopen_test.db"
    private val monday = LocalDate.of(2026, 11, 16)

    private fun repository(date: LocalDate): HabitRepositoryImpl {
        val db = EgoDatabaseProvider.getDatabase(context, name)
        return HabitRepositoryImpl(
            habitDao = db.habitDao(), habitOccurrenceDao = db.habitOccurrenceDao(),
            clock = Clock.fixed(date.atStartOfDay(habitZone).toInstant(), habitZone),
        )
    }

    @After
    fun cleanUp() {
        EgoDatabaseProvider.resetForTesting()
        context.deleteDatabase(name)
    }

    @Test
    fun midweekPreferenceEditsAndArchivingPreserveClosedDates() = runBlocking {
        val old = repository(monday)
        val created = monday.atStartOfDay(habitZone).toInstant().toEpochMilli()
        val oldGoal = HabitFrequency.TimesPerWeek(2, setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY))
        val id = old.saveHabit(Habit(name = "Correr", frequency = oldGoal, createdAt = created, updatedAt = created))
        val wednesday = monday.plusDays(2)
        val edited = repository(wednesday)
        val newGoal = HabitFrequency.TimesPerWeek(3, setOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY))
        edited.updateHabit(edited.getHabitById(id)!!.copy(frequency = newGoal))
        repository(monday.plusDays(5)).archiveHabit(id).getOrThrow()
        EgoDatabaseProvider.resetForTesting()

        val reopened = repository(monday.plusWeeks(1))
        val calendar = reopened.observeHabitCalendars().first().single()
        assertEquals(listOf(monday, monday.plusDays(4)), calendar.days(monday, monday.plusDays(6)).filter { it.expected }.map { it.date })
        assertEquals(listOf("0 de 3"), calendar.history(monday.plusWeeks(1)).map { it.subtitle })
        assertTrue(reopened.correctOccurrence(id, monday.minusDays(1), true).isFailure)
        assertTrue(reopened.correctOccurrence(id, monday.plusDays(6), true).isFailure)
        reopened.correctOccurrence(id, monday, true).getOrThrow()
        assertEquals("1 de 3", reopened.observeHabitCalendars().first().single().history(monday.plusWeeks(1)).single().subtitle)
    }

    @Test
    fun reopensAfterWeekBoundaryAndCorrectsOriginalDateBeyondGoal() = runBlocking {
        val original = repository(monday)
        val created = monday.atStartOfDay(habitZone).toInstant().toEpochMilli()
        val goal = HabitFrequency.TimesPerWeek(3, setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        val id = original.saveHabit(Habit(name = "Ler", frequency = goal, createdAt = created, updatedAt = created))
        for (offset in 1L..3L) {
            original.completeOccurrence(id, monday.plusDays(offset).toString(), "nota").getOrThrow()
        }
        assertTrue(original.completeOccurrence(id, monday.plusDays(4).toString()).isFailure)
        EgoDatabaseProvider.resetForTesting()

        val nextMonday = monday.plusWeeks(1)
        val reopened = repository(nextMonday)
        reopened.updateHabit(reopened.getHabitById(id)!!.copy(frequency = HabitFrequency.Daily))
        val before = reopened.observeHabitCalendars().first().single()
        val oldWeek = before.days(monday, monday.plusDays(6))
        assertEquals(listOf(monday, monday.plusDays(2)), oldWeek.filter { it.expected }.map { it.date })
        assertFalse(oldWeek.first().completed)
        assertTrue(oldWeek.first().missedOn(nextMonday))
        assertFalse(oldWeek.first { it.date == monday.plusDays(4) }.missedOn(nextMonday))
        assertEquals("3 de 3", before.history(nextMonday).single().subtitle)

        repeat(2) { reopened.correctOccurrence(id, monday, true).getOrThrow() }
        val corrected = reopened.observeHabitCalendars().first().single()
        assertEquals(4, corrected.occurrences.size)
        assertEquals("4 de 3", corrected.history(nextMonday).single().subtitle)
        assertEquals(monday, corrected.days(monday, monday).single { it.completed && it.expected }.date)
        assertEquals(4, HabitProgress.calculate(before.habit.copy(frequency = goal), corrected.occurrences, monday.plusDays(6)).completedThisWeek)

        val tuesday = monday.plusDays(1)
        reopened.correctOccurrence(id, tuesday, false).getOrThrow()
        assertEquals("nota", reopened.getOccurrenceForDate(id, tuesday.toString())?.notes)
        reopened.correctOccurrence(id, monday, false).getOrThrow()
        EgoDatabaseProvider.resetForTesting()
        val week = repository(nextMonday).observeHabitCalendars().first().single()
        val row = week.history(nextMonday).single()
        assertEquals("Meta não atingida", row.status)
        assertEquals("2 de 3", row.subtitle)
        assertTrue(week.days(monday.plusDays(4), monday.plusDays(4)).single().expected)
        assertFalse(week.days(monday, monday).single().completed)
    }
}
