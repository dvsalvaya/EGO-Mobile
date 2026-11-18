package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.repository.HaircareRepository
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class FakeConsolidationHabitRepository : HabitRepository {
    val habits = MutableStateFlow<List<Habit>>(emptyList())
    private val occurrences = mutableMapOf<Long, MutableStateFlow<List<HabitOccurrence>>>()
    val completionCalls = mutableListOf<CompleteHabitCall>()
    var completionResult: Result<Unit> = Result.success(Unit)

    fun setOccurrences(habitId: Long, values: List<HabitOccurrence>) {
        occurrences.getOrPut(habitId) { MutableStateFlow(emptyList()) }.value = values
    }

    override suspend fun saveHabit(habit: Habit): Long = error("Not used by consolidation tests")
    override suspend fun updateHabit(habit: Habit) = error("Not used by consolidation tests")
    override suspend fun deleteHabit(habit: Habit) = error("Not used by consolidation tests")
    override suspend fun getHabitById(id: Long): Habit? = error("Not used by consolidation tests")
    override fun observeAllHabits(): Flow<List<Habit>> = habits

    override suspend fun completeOccurrence(
        habitId: Long,
        dateCivil: String,
        notes: String?,
    ): Result<Unit> {
        completionCalls += CompleteHabitCall(habitId, dateCivil, notes)
        return completionResult
    }

    override suspend fun undoOccurrence(habitId: Long, dateCivil: String): Result<Unit> = Result.success(Unit)

    override suspend fun pauseHabit(habitId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun resumeHabit(habitId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun archiveHabit(habitId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun getOccurrenceForDate(habitId: Long, dateCivil: String): HabitOccurrence? =
        occurrences[habitId]?.value?.firstOrNull { it.date.toString() == dateCivil }

    override fun observeOccurrencesForHabit(habitId: Long): Flow<List<HabitOccurrence>> =
        occurrences.getOrPut(habitId) { MutableStateFlow(emptyList()) }
}

internal data class CompleteHabitCall(
    val habitId: Long,
    val dateCivil: String,
    val notes: String?,
)

internal class FakeConsolidationTaskRepository : TaskRepository {
    val openTasks = MutableStateFlow<List<Task>>(emptyList())
    val completedTasks = MutableStateFlow<List<Task>>(emptyList())
    val completionCalls = mutableListOf<CompleteTaskCall>()
    var completionResult: Result<Unit> = Result.success(Unit)

    override suspend fun createTask(task: Task): Long = error("Not used by consolidation tests")
    override suspend fun updateTask(task: Task) = error("Not used by consolidation tests")
    override suspend fun getTaskById(id: Long): Task? = error("Not used by consolidation tests")
    override fun getOpenTasks(): Flow<List<Task>> = openTasks
    override fun getCompletedTasks(): Flow<List<Task>> = completedTasks
    override suspend fun completeTask(id: Long, completedAt: Long): Result<Unit> {
        completionCalls += CompleteTaskCall(id, completedAt)
        return completionResult
    }

    override suspend fun rescheduleTask(id: Long, newDueAt: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun deleteTask(id: Long): Result<Task> =
        Result.failure(UnsupportedOperationException())

    override suspend fun restoreTask(task: Task): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

internal data class CompleteTaskCall(
    val taskId: Long,
    val completedAt: Long,
)

internal class FakeConsolidationWorkoutRepository : WorkoutRepository {
    val activeSession = MutableStateFlow<WorkoutSession?>(null)
    val completedSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())

    override fun getActiveSession(): Flow<WorkoutSession?> = activeSession
    override suspend fun getActiveSessionSync(): WorkoutSession? = activeSession.value
    override fun getCompletedSessions(): Flow<List<WorkoutSession>> = completedSessions
    override fun getSessionById(sessionId: Long): Flow<WorkoutSession?> = flowOf(null)
    override suspend fun startSession(title: String, startedAt: Long): Result<WorkoutSession> =
        Result.failure(UnsupportedOperationException())

    override suspend fun pauseSession(sessionId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun resumeSession(sessionId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun completeSession(sessionId: Long, endedAt: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun cancelSession(sessionId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun deleteSession(sessionId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun addExercise(exercise: WorkoutExercise): Result<Long> =
        Result.failure(UnsupportedOperationException())

    override suspend fun updateExercise(exercise: WorkoutExercise): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun removeExercise(exerciseId: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

internal class FakeConsolidationHaircareRepository : HaircareRepository {
    val activeRoutine = MutableStateFlow<HaircareRoutine?>(null)
    val completedOccurrences = MutableStateFlow<List<HaircareOccurrence>>(emptyList())
    private val occurrences = mutableMapOf<Long, MutableStateFlow<List<HaircareOccurrence>>>()

    fun setOccurrences(routineId: Long, values: List<HaircareOccurrence>) {
        occurrences.getOrPut(routineId) { MutableStateFlow(emptyList()) }.value = values
    }

    override fun observeActiveRoutine(): Flow<HaircareRoutine?> = activeRoutine
    override fun observeOccurrencesForRoutine(routineId: Long): Flow<List<HaircareOccurrence>> =
        occurrences.getOrPut(routineId) { MutableStateFlow(emptyList()) }

    override fun observeNextPendingOccurrence(routineId: Long): Flow<HaircareOccurrence?> =
        observeOccurrencesForRoutine(routineId).map { it.firstOrNull() }

    override fun observeCompletedOccurrences(): Flow<List<HaircareOccurrence>> = completedOccurrences
    override suspend fun createRoutine(
        routine: HaircareRoutine,
        occurrences: List<HaircareOccurrence>,
    ): Result<Long> = Result.failure(UnsupportedOperationException())

    override suspend fun recordExecution(
        occurrenceId: Long,
        timestamp: Long,
        notes: String?,
    ): Result<Unit> = Result.failure(UnsupportedOperationException())

    override suspend fun rescheduleStep(occurrenceId: Long, newDateCivil: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}
