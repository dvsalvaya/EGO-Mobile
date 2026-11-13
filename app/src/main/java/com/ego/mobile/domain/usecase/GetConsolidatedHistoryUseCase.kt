package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HistoryItem
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.repository.HaircareRepository
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetConsolidatedHistoryUseCase(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val workoutRepository: WorkoutRepository,
    private val haircareRepository: HaircareRepository,
) {
    operator fun invoke(): Flow<List<HistoryItem>> = combine(
        habitRepository.observeHabitOccurrenceSnapshot(),
        taskRepository.getCompletedTasks(),
        workoutRepository.getCompletedSessions(),
        haircareRepository.observeCompletedOccurrences(),
    ) { habitSnapshot, tasks, workouts, haircare ->
        buildList {
            habitSnapshot.habits.forEach { habit ->
                habitSnapshot.occurrencesByHabit[habit.id]
                    .orEmpty()
                    .filter { it.status == HabitOccurrenceStatus.COMPLETED }
                    .forEach { occurrence ->
                        occurrence.completedAt?.let { completedAt ->
                            add(
                                HistoryItem(
                                    occurredAt = completedAt,
                                    type = ActivityType.HABIT,
                                    title = habit.name,
                                    subtitle = occurrence.notes?.takeIf(String::isNotBlank),
                                    status = HabitOccurrenceStatus.COMPLETED.name,
                                ),
                            )
                        }
                    }
            }
            tasks.filter { it.status == TaskStatus.COMPLETED }.forEach { task ->
                task.completedAt?.let { completedAt ->
                    add(
                        HistoryItem(
                            occurredAt = completedAt,
                            type = ActivityType.TASK,
                            title = task.title,
                            subtitle = task.description?.takeIf(String::isNotBlank),
                            status = TaskStatus.COMPLETED.name,
                        ),
                    )
                }
            }
            workouts.filter { it.status == WorkoutSessionStatus.COMPLETED }.forEach { workout ->
                workout.endedAt?.let { endedAt ->
                    add(
                        HistoryItem(
                            occurredAt = endedAt,
                            type = ActivityType.WORKOUT,
                            title = workout.title,
                            subtitle = workout.notes?.takeIf(String::isNotBlank),
                            status = WorkoutSessionStatus.COMPLETED.name,
                        ),
                    )
                }
            }
            haircare.filter { it.status == HaircareOccurrenceStatus.COMPLETED }.forEach { occurrence ->
                occurrence.executedAt?.let { executedAt ->
                    val subtitle = listOfNotNull(
                        occurrence.stepType.displayName,
                        occurrence.notes?.takeIf(String::isNotBlank),
                    ).joinToString(" · ")
                    add(
                        HistoryItem(
                            occurredAt = executedAt,
                            type = ActivityType.HAIRCARE,
                            title = occurrence.stepTitle,
                            subtitle = subtitle,
                            status = HaircareOccurrenceStatus.COMPLETED.name,
                        ),
                    )
                }
            }
        }.sortedByDescending(HistoryItem::occurredAt)
    }
}
