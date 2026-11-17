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
import com.ego.mobile.domain.usecase.ObserveHabitProgressUseCase
import com.ego.mobile.domain.usecase.UpdateHabitFrequencyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class HabitsViewModel(
    private val repository: HabitRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
    private val progressUseCase: ObserveHabitProgressUseCase = ObserveHabitProgressUseCase(repository, today),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private val updateHabitFrequencyUseCase = UpdateHabitFrequencyUseCase()

    init {
        combine(repository.observeAllHabits(), progressUseCase()) { habits, progress ->
            val visible = habits.filter { it.status != HabitStatus.ARCHIVED }
            HabitsUiState(
                activeHabits = visible.filter { it.status == HabitStatus.ACTIVE }
                    .sortedByDescending { it.updatedAt },
                pausedHabits = visible.filter { it.status == HabitStatus.PAUSED }
                    .sortedByDescending { it.updatedAt },
                progressByHabit = progress,
                isLoading = false,
                errorMessage = null,
                operationMessage = _uiState.value.operationMessage,
            )
        }
            .onStart { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) }
            .onEach { state -> _uiState.value = state }
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
            val progress = progressUseCase().first()[habitId]
            if (progress != null && !progress.canComplete) {
                setOperationMessage("Meta semanal já atingida para este hábito.")
                return@launch
            }
            runCatching { repository.completeOccurrence(habitId, today().toString(), null) }
                .onSuccess { result ->
                    if (result.isFailure) {
                        setOperationMessage(result.exceptionOrNull()?.message ?: "Não foi possível concluir o hábito.")
                    } else {
                        setOperationMessage("Hábito concluído.")
                    }
                }
                .onFailure { setOperationMessage(it.message ?: "Não foi possível concluir o hábito.") }
        }
    }

    fun onUndoCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.revertOccurrence(habitId, today().toString())
                .onSuccess { setOperationMessage("Conclusão desfeita.") }
                .onFailure { setOperationMessage(it.message ?: "Não foi possível desfazer a conclusão.") }
        }
    }

    fun onPauseHabit(habitId: Long) = changeStatus(habitId, HabitStatus.PAUSED, "Hábito pausado.")

    fun onResumeHabit(habitId: Long) = changeStatus(habitId, HabitStatus.ACTIVE, "Hábito retomado.")

    fun onArchiveHabit(habitId: Long) = changeStatus(habitId, HabitStatus.ARCHIVED, "Hábito arquivado.")

    fun onCreateHabit(name: String, description: String?, frequency: HabitFrequency) {
        viewModelScope.launch {
            val habit = runCatching {
                Habit(
                    name = name.trim(),
                    description = description?.trim()?.ifEmpty { null },
                    frequency = frequency,
                    status = HabitStatus.ACTIVE,
                    createdAt = clock(),
                    updatedAt = clock(),
                )
            }.getOrElse {
                setOperationMessage(it.message ?: "Verifique os dados do hábito.")
                return@launch
            }
            runCatching { repository.saveHabit(habit) }
                .onSuccess { setOperationMessage("Hábito criado.") }
                .onFailure { setOperationMessage(it.message ?: "Não foi possível salvar o hábito.") }
        }
    }

    fun onEditHabit(habitId: Long, name: String, description: String?) {
        viewModelScope.launch {
            val existing = repository.getHabitById(habitId)
                ?: return@launch setOperationMessage("Hábito não encontrado.")
            val edited = runCatching {
                existing.copy(
                    name = name.trim(),
                    description = description?.trim()?.ifEmpty { null },
                    updatedAt = clock(),
                )
            }.getOrElse {
                setOperationMessage(it.message ?: "Verifique os dados do hábito.")
                return@launch
            }
            runCatching { repository.updateHabit(edited) }
                .onSuccess { setOperationMessage("Hábito atualizado.") }
                .onFailure { setOperationMessage(it.message ?: "Não foi possível atualizar o hábito.") }
        }
    }

    fun onUpdateFrequency(habitId: Long, newFrequency: HabitFrequency) {
        viewModelScope.launch {
            val existingHabit = repository.getHabitById(habitId)
                ?: return@launch setOperationMessage("Hábito não encontrado.")
            val occurrences = repository.observeOccurrencesForHabit(habitId).first()
            runCatching {
                updateHabitFrequencyUseCase.invoke(
                    habit = existingHabit,
                    newFrequency = newFrequency,
                    effectiveDate = today(),
                    existingOccurrences = occurrences,
                )
            }.onSuccess { result ->
                runCatching { repository.updateHabit(result.habit.copy(updatedAt = clock())) }
                    .onSuccess { setOperationMessage("Frequência atualizada; histórico preservado.") }
                    .onFailure { setOperationMessage(it.message ?: "Não foi possível alterar a frequência.") }
            }.onFailure {
                setOperationMessage(it.message ?: "Não foi possível alterar a frequência.")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearOperationMessage() {
        _uiState.value = _uiState.value.copy(operationMessage = null)
    }

    private fun changeStatus(habitId: Long, status: HabitStatus, successMessage: String) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId)
                ?: return@launch setOperationMessage("Hábito não encontrado.")
            runCatching {
                repository.updateHabit(habit.copy(status = status, updatedAt = clock()))
            }.onSuccess { setOperationMessage(successMessage) }
                .onFailure { setOperationMessage(it.message ?: "Não foi possível alterar o hábito.") }
        }
    }

    private fun setOperationMessage(message: String) {
        _uiState.value = _uiState.value.copy(operationMessage = message)
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
