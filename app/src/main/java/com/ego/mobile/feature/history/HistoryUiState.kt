package com.ego.mobile.feature.history

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HistoryItem

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val selectedFilter: ActivityType? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val visibleItems: List<HistoryItem>
        get() = items.filter { selectedFilter == null || it.type == selectedFilter }
}
