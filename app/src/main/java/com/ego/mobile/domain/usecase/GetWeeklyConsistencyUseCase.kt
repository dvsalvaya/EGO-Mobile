package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.ConsistencyEvent
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.model.WeeklyConsistency
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Observa conclusões reais e produz a grade de consistência dos últimos sete dias locais. */
@OptIn(ExperimentalCoroutinesApi::class)
class GetWeeklyConsistencyUseCase(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val workoutRepository: WorkoutRepository,
    private val zoneId: ZoneId = ZoneId.of("America/Sao_Paulo"),
    private val today: () -> LocalDate = { LocalDate.now(zoneId) },
) {
    operator fun invoke(): Flow<WeeklyConsistency> = combine(
        habitRepository.observeHabitOccurrenceSnapshot(),
        taskRepository.getCompletedTasks(),
        workoutRepository.getCompletedSessions(),
    ) { habitSnapshot, tasks, sessions ->
        val events = buildList {
            habitSnapshot.occurrencesByHabit.values.flatten()
                .filter { it.status == HabitOccurrenceStatus.COMPLETED }
                .forEach { occurrence ->
                    add(
                        ConsistencyEvent(
                            sourceType = ActivityType.HABIT,
                            sourceId = occurrence.id,
                            date = occurrence.date,
                        ),
                    )
                }
            tasks.filter { it.status == TaskStatus.COMPLETED && it.completedAt != null }
                .forEach { task ->
                    add(
                        ConsistencyEvent(
                            sourceType = ActivityType.TASK,
                            sourceId = task.id,
                            date = localDateOf(task.completedAt!!),
                        ),
                    )
                }
            sessions.filter { it.status == WorkoutSessionStatus.COMPLETED && it.endedAt != null }
                .forEach { session ->
                    add(
                        ConsistencyEvent(
                            sourceType = ActivityType.WORKOUT,
                            sourceId = session.id,
                            date = localDateOf(session.endedAt!!),
                        ),
                    )
                }
        }
        aggregateWeeklyConsistency(events, today())
    }

    private fun localDateOf(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
}
