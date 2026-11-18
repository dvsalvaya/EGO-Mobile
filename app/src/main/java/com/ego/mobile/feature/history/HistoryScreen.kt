package com.ego.mobile.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ego.mobile.feature.history.components.HistoryFilterChips
import com.ego.mobile.feature.history.components.HistoryTimelineItem

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = rememberHistoryViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            Text(
                text = "Histórico",
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.headlineSmall,
            )
            HistoryFilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = viewModel::setFilter,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when {
                    uiState.isLoading -> Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                    uiState.errorMessage != null && uiState.visibleItems.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        uiState.errorMessage?.let { message ->
                            Text(message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    uiState.visibleItems.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = if (uiState.items.isEmpty()) {
                                "Nenhuma atividade registrada."
                            } else {
                                "Nenhuma atividade encontrada para este filtro."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> Column(modifier = Modifier.fillMaxSize()) {
                        uiState.errorMessage?.let { message ->
                            Text(
                                text = message,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.visibleItems) { item ->
                                HistoryTimelineItem(item = item, onCorrect = viewModel::correctOccurrence)
                            }
                        }
                    }
                }
            }
        }
    }
}
