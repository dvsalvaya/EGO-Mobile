package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HomeSummary
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.repository.HaircareRepository
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GetHomeSummaryUseCase(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val workoutRepository: WorkoutRepository,
    private val haircareRepository: HaircareRepository,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
) {
    operator fun invoke(): Flow<HomeSummary> {
        val todayHabits = habitRepository.observeHabitOccurrenceSnapshot().map { snapshot ->
            val date = today()
            snapshot.habits
                .asSequence()
                .filter { habit ->
                    habit.status == HabitStatus.ACTIVE && habit.frequency.requiresOccurrenceOn(date)
                }
                .mapIndexed { index, habit ->
                    val occurrences = snapshot.occurrencesByHabit[habit.id].orEmpty()
                    val occurrence = occurrences.firstOrNull { it.date == date }
                    val progress = HabitProgress.calculate(habit, occurrences, date)
                    if (occurrence?.status == HabitOccurrenceStatus.COMPLETED || !progress.canCompleteToday) {
                        return@mapIndexed null
                    }
                    val priority = when (val frequency = habit.frequency) {
                        HabitFrequency.Daily, is HabitFrequency.WeeklyDays -> 0
                        is HabitFrequency.TimesPerWeek -> if (date.dayOfWeek in frequency.preferredDays) 0 else 1
                    }
                    priority to (index to HomeSummary.TodayHabit(
                        habitId = habit.id,
                        title = habit.name,
                        status = occurrence?.status ?: HabitOccurrenceStatus.PENDING,
                        completedThisWeek = progress.completedThisWeek,
                        weeklyTarget = progress.weeklyTarget,
                        canComplete = progress.canCompleteToday,
                    ))
                }
                .filterNotNull()
                .sortedWith(compareBy({ it.first }, { it.second.first }))
                .map { it.second.second }
                .toList()
        }
        val haircareRoutine = haircareRepository.observeActiveRoutine().flatMapLatest { routine ->
            if (routine == null) {
                flowOf(null)
            } else {
                haircareRepository.observeOccurrencesForRoutine(routine.id).map { occurrences ->
                    occurrences
                        .asSequence()
                        .filter {
                            it.status == HaircareOccurrenceStatus.PENDING ||
                                it.status == HaircareOccurrenceStatus.RESCHEDULED
                        }
                        .minWithOrNull(compareBy({ it.scheduledDate }, { it.orderIndex }))
                        ?.let { nextStep ->
                            HomeSummary.ActiveRoutine(
                                id = routine.id,
                                type = ActivityType.HAIRCARE,
                                title = routine.name,
                                subtitle = "${nextStep.stepTitle} · ${nextStep.scheduledDate}",
                            )
                        }
                }
            }
        }

        return combine(
            todayHabits,
            taskRepository.getOpenTasks(),
            workoutRepository.getActiveSession(),
            haircareRoutine,
        ) { habits, tasks, workout, haircare ->
            val activeWorkout = workout
                ?.takeIf { it.status.isActive }
                ?.let { session ->
                    HomeSummary.ActiveRoutine(
                        id = session.id,
                        type = ActivityType.WORKOUT,
                        title = session.title,
                        subtitle = when (session.status) {
                            WorkoutSessionStatus.IN_PROGRESS -> "Treino em andamento"
                            WorkoutSessionStatus.PAUSED -> "Treino pausado"
                            else -> null
                        },
                    )
                }

            val eligibleTasks = tasks.filter { task ->
                task.dueAt?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate() <= today() } == true
            }.sortedBy { task ->
                task.dueAt?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate() }
            }
            HomeSummary(
                todayHabits = habits.take(3),
                todayHabitsTotal = habits.size,
                priorityTasks = eligibleTasks.take(3),
                priorityTasksTotal = eligibleTasks.size,
                activeRoutine = activeWorkout ?: haircare,
                activeWorkout = activeWorkout,
                haircareRoutine = haircare,
            )
        }
    }
}
