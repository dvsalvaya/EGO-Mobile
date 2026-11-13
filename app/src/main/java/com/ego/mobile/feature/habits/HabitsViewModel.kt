package com.ego.mobile.feature.habits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ego.mobile.data.local.EgoDatabaseProvider
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.usecase.UpdateHabitFrequencyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class HabitsViewModel(
    private val repository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private val updateHabitFrequencyUseCase = UpdateHabitFrequencyUseCase()

    init {
        repository.observeAllHabits()
            .onStart { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) }
            .onEach { habits ->
                val active = habits.filter { it.status == HabitStatus.ACTIVE }
                val paused = habits.filter { it.status == HabitStatus.PAUSED }
                _uiState.value = HabitsUiState(
                    activeHabits = active.sortedByDescending { it.updatedAt },
                    pausedHabits = paused.sortedByDescending { it.updatedAt },
                    isLoading = false,
                    errorMessage = null,
                )
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Não foi possível carregar os hábitos.",
                )
            }
            .launchIn(viewModelScope)
    }

    fun onQuickComplete(habitId: Long) {
        viewModelScope.launch {
            val result = repository.completeOccurrence(habitId, todayCivilDate(), null)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Não foi possível concluir o hábito.",
                )
            }
        }
    }

    fun onPauseHabit(habitId: Long) {
        viewModelScope.launch {
            val result = repository.pauseHabit(habitId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Não foi possível pausar o hábito.",
                )
            }
        }
    }

    fun onResumeHabit(habitId: Long) {
        viewModelScope.launch {
            val result = repository.resumeHabit(habitId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Não foi possível retomar o hábito.",
                )
            }
        }
    }

    fun onCreateHabit(name: String, description: String?, frequency: HabitFrequency) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            val habit = Habit(
                name = trimmedName,
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                frequency = frequency,
                status = HabitStatus.ACTIVE,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            repository.saveHabit(habit)
        }
    }

    fun onUpdateFrequency(habitId: Long, newFrequency: HabitFrequency) {
        viewModelScope.launch {
            val existingHabit = repository.getHabitById(habitId) ?: return@launch
            val occurrences = repository.observeOccurrencesForHabit(habitId).first()
            val result = updateHabitFrequencyUseCase.invoke(
                habit = existingHabit,
                newFrequency = newFrequency,
                effectiveDate = LocalDate.now(ZoneId.of("America/Sao_Paulo")),
                existingOccurrences = occurrences,
            )
            repository.updateHabit(result.habit)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun todayCivilDate(): String = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString()
}

@Composable
fun rememberHabitsViewModel(): HabitsViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val database = EgoDatabaseProvider.getDatabase(context)
        val repository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        HabitsViewModel(repository)
    }
}
