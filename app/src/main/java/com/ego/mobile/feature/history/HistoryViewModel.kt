package com.ego.mobile.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ego.mobile.data.local.EgoDatabaseProvider
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.data.repository.HaircareRepositoryImpl
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.usecase.GetConsolidatedHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class HistoryViewModel(
    getConsolidatedHistoryUseCase: GetConsolidatedHistoryUseCase,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow<ActivityType?>(null)
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        combine(getConsolidatedHistoryUseCase(), selectedFilter) { items, filter ->
            HistoryUiState(
                items = items.sortedByDescending { it.occurredAt },
                selectedFilter = filter,
                isLoading = false,
            )
        }
            .onStart { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Não foi possível carregar o histórico.",
                )
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun setFilter(type: ActivityType?) {
        selectedFilter.value = type
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

@Composable
fun rememberHistoryViewModel(): HistoryViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val database = EgoDatabaseProvider.getDatabase(context)
        val habitRepository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val taskRepository = TaskRepositoryImpl(database.taskDao())
        val workoutRepository = WorkoutRepositoryImpl(database.workoutDao())
        val haircareRepository = HaircareRepositoryImpl(database.haircareDao())
        HistoryViewModel(
            getConsolidatedHistoryUseCase = GetConsolidatedHistoryUseCase(
                habitRepository = habitRepository,
                taskRepository = taskRepository,
                workoutRepository = workoutRepository,
                haircareRepository = haircareRepository,
            ),
        )
    }
}
