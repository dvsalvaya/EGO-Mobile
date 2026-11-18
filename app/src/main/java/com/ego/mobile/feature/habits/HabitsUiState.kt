package com.ego.mobile.feature.habits

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitProgress

data class HabitsUiState(
    val activeHabits: List<Habit> = emptyList(),
    val pausedHabits: List<Habit> = emptyList(),
    val progressByHabitId: Map<Long, HabitProgress> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = activeHabits.isEmpty() && pausedHabits.isEmpty()
}
