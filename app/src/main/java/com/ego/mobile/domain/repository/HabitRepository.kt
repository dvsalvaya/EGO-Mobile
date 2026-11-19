package com.ego.mobile.domain.repository

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitCalendar
import com.ego.mobile.domain.model.HabitOccurrence
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun saveHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun getHabitById(id: Long): Habit?
    fun observeAllHabits(): Flow<List<Habit>>
    fun observeHabitCalendars(): Flow<List<HabitCalendar>>
    suspend fun correctOccurrence(habitId: Long, date: LocalDate, completed: Boolean): Result<Unit>
    suspend fun completeOccurrence(habitId: Long, dateCivil: String, notes: String? = null): Result<Unit>
    suspend fun undoOccurrence(habitId: Long, dateCivil: String): Result<Unit>
    suspend fun pauseHabit(habitId: Long): Result<Unit>
    suspend fun resumeHabit(habitId: Long): Result<Unit>
    suspend fun archiveHabit(habitId: Long): Result<Unit>
    suspend fun getOccurrenceForDate(habitId: Long, dateCivil: String): HabitOccurrence?
    fun observeOccurrencesForHabit(habitId: Long): Flow<List<HabitOccurrence>>
}
