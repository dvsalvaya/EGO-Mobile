package com.ego.mobile.feature.habits

import com.ego.mobile.domain.model.Habit

data class HabitsUiState(
    val activeHabits: List<Habit> = emptyList(),
    val pausedHabits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = activeHabits.isEmpty() && pausedHabits.isEmpty()
}
