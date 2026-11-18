package com.ego.mobile.feature.home

import com.ego.mobile.domain.model.HomeSummary
import com.ego.mobile.domain.model.HabitDayGrid

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Content(
        val summary: HomeSummary,
        val dayGrid: HabitDayGrid,
        val errorMessage: String? = null,
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}
