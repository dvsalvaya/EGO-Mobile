package com.ego.mobile.feature.routines.haircare

import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareRoutine

sealed interface HaircareUiState {
    data object Loading : HaircareUiState

    data class NoRoutineConfigured(
        val errorMessage: String? = null,
    ) : HaircareUiState

    data class ActiveRoutine(
        val routine: HaircareRoutine,
        val currentStep: HaircareOccurrence?,
        val upcomingSteps: List<HaircareOccurrence>,
        val history: List<HaircareOccurrence>,
        val errorMessage: String? = null,
    ) : HaircareUiState

    data class Error(val message: String) : HaircareUiState
}
