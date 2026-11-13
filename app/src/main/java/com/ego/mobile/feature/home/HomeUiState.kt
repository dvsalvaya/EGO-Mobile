package com.ego.mobile.feature.home

import com.ego.mobile.domain.model.HomeSummary

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Content(
        val summary: HomeSummary,
        val errorMessage: String? = null,
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}
