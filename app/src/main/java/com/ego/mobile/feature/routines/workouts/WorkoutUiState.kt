package com.ego.mobile.feature.routines.workouts

import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.usecase.GetWorkoutStatisticsUseCase

data class WorkoutUiState(
    val activeSession: WorkoutSession? = null,
    val completedSessions: List<WorkoutSession> = emptyList(),
    val stats: GetWorkoutStatisticsUseCase.Statistics = GetWorkoutStatisticsUseCase.Statistics(0, 0.0, emptyList()),
    val elapsedTimeMillis: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasActiveSession: Boolean
        get() = activeSession != null
}

sealed interface WorkoutEvent {
    data class ShowMessage(val message: String) : WorkoutEvent
    data class Error(val error: String) : WorkoutEvent
    data object SessionStarted : WorkoutEvent
    data object SessionCompleted : WorkoutEvent
    data object SessionDiscarded : WorkoutEvent
}
