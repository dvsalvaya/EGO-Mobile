package com.ego.mobile.feature.history.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.ActivityType

@Composable
fun HistoryFilterChips(
    selectedFilter: ActivityType?,
    onFilterSelected: (ActivityType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = listOf(
        null to "Todas",
        ActivityType.HABIT to "Hábitos",
        ActivityType.TASK to "Tarefas",
        ActivityType.WORKOUT to "Treinos",
        ActivityType.HAIRCARE to "Cuidados",
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { (type, label) ->
            FilterChip(
                selected = selectedFilter == type,
                onClick = { onFilterSelected(type) },
                label = { Text(label) },
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            )
        }
    }
}
