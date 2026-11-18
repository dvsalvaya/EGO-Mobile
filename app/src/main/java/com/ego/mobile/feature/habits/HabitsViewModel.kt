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
import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.usecase.observeHabitOccurrenceSnapshot
import com.ego.mobile.domain.usecase.UpdateHabitFrequencyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class HabitsViewModel(
    private val repository: HabitRepository,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private val updateHabitFrequencyUseCase = UpdateHabitFrequencyUseCase()

    init {
        repository.observeHabitOccurrenceSnapshot()
            .onStart { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) }
            .onEach { snapshot ->
                val habits = snapshot.habits
                val active = habits.filter { it.status == HabitStatus.ACTIVE }
                val paused = habits.filter { it.status == HabitStatus.PAUSED }
                _uiState.value = HabitsUiState(
                    activeHabits = active.sortedByDescending { it.updatedAt },
                    pausedHabits = paused.sortedByDescending { it.updatedAt },
                    progressByHabitId = habits.associate { habit ->
                        habit.id to HabitProgress.calculate(
                            habit,
                            snapshot.occurrencesByHabit[habit.id].orEmpty(),
                            today(),
                        )
                    },
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
            val result = repository.completeOccurrence(habitId, today().toString(), null)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Não foi possível concluir o hábito.",
                )
            }
        }
    }

    fun onUndoCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.undoOccurrence(habitId, today().toString()).onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "NÃ£o foi possÃ­vel desfazer a conclusÃ£o.",
                )
            }
        }
    }

    fun onUpdateHabitDetails(habitId: Long, name: String, description: String?) {
        viewModelScope.launch {
            val existing = repository.getHabitById(habitId) ?: return@launch
            runCatching {
                existing.copy(
                    name = name.trim(),
                    description = description?.trim()?.takeIf(String::isNotEmpty),
                    updatedAt = System.currentTimeMillis(),
                )
            }.onSuccess { updated ->
                runCatching { repository.updateHabit(updated) }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Nao foi possivel atualizar o habito.",
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Verifique os dados do habito.",
                )
            }
        }
    }

    fun onArchiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.archiveHabit(habitId).onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "NÃ£o foi possÃ­vel arquivar o hÃ¡bito.",
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
            runCatching {
                val now = System.currentTimeMillis()
                Habit(
                    name = name.trim(),
                    description = description?.trim()?.takeIf { it.isNotEmpty() },
                    frequency = frequency,
                    status = HabitStatus.ACTIVE,
                    createdAt = now,
                    updatedAt = now,
                )
            }.onSuccess { habit ->
                runCatching { repository.saveHabit(habit) }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message ?: "NÃ£o foi possÃ­vel salvar o hÃ¡bito.")
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(errorMessage = error.message ?: "Verifique os dados do hÃ¡bito.")
            }
        }
    }

    fun onUpdateFrequency(habitId: Long, newFrequency: HabitFrequency) {
        viewModelScope.launch {
            val existingHabit = repository.getHabitById(habitId) ?: return@launch
            val occurrences = repository.observeOccurrencesForHabit(habitId).first()
            val result = updateHabitFrequencyUseCase.invoke(
                habit = existingHabit,
                newFrequency = newFrequency,
                effectiveDate = today(),
                existingOccurrences = occurrences,
            )
            runCatching { repository.updateHabit(result.habit) }.onFailure { error ->
                _uiState.value = _uiState.value.copy(errorMessage = error.message ?: "NÃ£o foi possÃ­vel alterar a frequÃªncia.")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

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
