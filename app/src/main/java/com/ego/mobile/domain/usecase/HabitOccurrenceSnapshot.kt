package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal data class HabitOccurrenceSnapshot(
    val habits: List<Habit>,
    val occurrencesByHabit: Map<Long, List<HabitOccurrence>>,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal fun HabitRepository.observeHabitOccurrenceSnapshot(): Flow<HabitOccurrenceSnapshot> =
    observeAllHabits().flatMapLatest { habits ->
        if (habits.isEmpty()) {
            flowOf(HabitOccurrenceSnapshot(emptyList(), emptyMap()))
        } else {
            combine(habits.map { observeOccurrencesForHabit(it.id) }) { occurrenceGroups ->
                HabitOccurrenceSnapshot(
                    habits = habits,
                    occurrencesByHabit = habits.indices.associate { index ->
                        habits[index].id to occurrenceGroups[index]
                    },
                )
            }
        }
    }
