package com.ego.mobile.feature.routines.haircare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ego.mobile.data.local.EgoDatabaseProvider
import com.ego.mobile.data.repository.HaircareRepositoryImpl
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircarePreset
import com.ego.mobile.domain.model.HaircarePresetStep
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.HaircareRoutineSource
import com.ego.mobile.domain.repository.HaircareRepository
import com.ego.mobile.domain.usecase.RecordHaircareExecutionUseCase
import com.ego.mobile.domain.usecase.RescheduleHaircareStepUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HaircareViewModel(
    private val repository: HaircareRepository,
    private val recordExecutionUseCase: RecordHaircareExecutionUseCase =
        RecordHaircareExecutionUseCase(repository),
    private val rescheduleStepUseCase: RescheduleHaircareStepUseCase =
        RescheduleHaircareStepUseCase(repository),
    private val clock: () -> Long = System::currentTimeMillis,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
) : ViewModel() {

    private val _uiState = MutableStateFlow<HaircareUiState>(HaircareUiState.Loading)
    val uiState: StateFlow<HaircareUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<HaircareEvent>(Channel.BUFFERED)
    val events: Flow<HaircareEvent> = eventChannel.receiveAsFlow()

    init {
        repository.observeActiveRoutine()
            .flatMapLatest { routine ->
                if (routine == null) {
                    flowOf(HaircareUiState.NoRoutineConfigured())
                } else {
                    combine(
                        repository.observeOccurrencesForRoutine(routine.id),
                        repository.observeCompletedOccurrences(),
                    ) { occurrences, completed ->
                        val scheduled = occurrences
                            .filter {
                                it.status == HaircareOccurrenceStatus.PENDING ||
                                    it.status == HaircareOccurrenceStatus.RESCHEDULED
                            }
                            .sortedWith(compareBy<HaircareOccurrence>({ it.scheduledDate }, { it.orderIndex }))
                        HaircareUiState.ActiveRoutine(
                            routine = routine,
                            currentStep = scheduled.firstOrNull(),
                            upcomingSteps = scheduled.drop(1),
                            history = completed.filter { it.routineId == routine.id },
                        )
                    }
                }
            }
            .catch { error ->
                _uiState.value = HaircareUiState.Error(
                    error.message ?: "Não foi possível carregar a rotina capilar.",
                )
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun adoptStandardPreset(startDate: LocalDate = today()) {
        createRoutine(
            name = HaircarePreset.STANDARD_NAME,
            source = HaircareRoutineSource.PRESET,
            description = HaircarePreset.STANDARD_DESCRIPTION,
            startDate = startDate,
            steps = HaircarePreset.standardSteps(),
        )
    }

    fun createCustomRoutine(
        name: String,
        steps: List<HaircarePresetStep>,
        startDate: LocalDate = today(),
        description: String? = null,
    ) {
        createRoutine(
            name = name,
            source = HaircareRoutineSource.CUSTOM,
            description = description,
            startDate = startDate,
            steps = steps,
        )
    }

    fun recordExecution(occurrenceId: Long, notes: String? = null) {
        viewModelScope.launch {
            recordExecutionUseCase(occurrenceId, clock(), notes)
                .onFailure { error -> setError(error.message ?: "Não foi possível registrar a execução.") }
        }
    }

    fun rescheduleStep(occurrenceId: Long, newDateCivil: String) {
        viewModelScope.launch {
            rescheduleStepUseCase(occurrenceId, newDateCivil)
                .onFailure { error -> setError(error.message ?: "Não foi possível reagendar a etapa.") }
        }
    }

    fun clearError() {
        when (val state = _uiState.value) {
            is HaircareUiState.ActiveRoutine -> _uiState.value = state.copy(errorMessage = null)
            is HaircareUiState.NoRoutineConfigured -> _uiState.value = state.copy(errorMessage = null)
            is HaircareUiState.Error -> Unit
            HaircareUiState.Loading -> Unit
        }
    }

    private fun createRoutine(
        name: String,
        source: HaircareRoutineSource,
        description: String?,
        startDate: LocalDate,
        steps: List<HaircarePresetStep>,
    ) {
        val routine = try {
            HaircareRoutine(
                name = name.trim(),
                source = source,
                description = description?.trim()?.ifBlank { null },
                createdAt = clock(),
                updatedAt = clock(),
            )
        } catch (error: IllegalArgumentException) {
            setError(error.message ?: "Verifique os dados da rotina.")
            return
        }
        val occurrences = steps.mapIndexed { index, step ->
            HaircareOccurrence(
                stepType = step.stepType,
                stepTitle = step.stepTitle,
                scheduledDate = startDate.plusDays(step.dayOffset.toLong()).toString(),
                orderIndex = index,
            )
        }

        viewModelScope.launch {
            repository.createRoutine(routine, occurrences)
                .onSuccess { eventChannel.send(HaircareEvent.RoutineCreated) }
                .onFailure { error -> setError(error.message ?: "Não foi possível salvar a rotina.") }
        }
    }

    private fun setError(message: String) {
        _uiState.value = when (val state = _uiState.value) {
            is HaircareUiState.ActiveRoutine -> state.copy(errorMessage = message)
            is HaircareUiState.NoRoutineConfigured -> state.copy(errorMessage = message)
            is HaircareUiState.Error -> state
            HaircareUiState.Loading -> HaircareUiState.Error(message)
        }
    }
}

sealed interface HaircareEvent {
    data object RoutineCreated : HaircareEvent
}

@Composable
fun rememberHaircareViewModel(): HaircareViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val database = EgoDatabaseProvider.getDatabase(context)
        HaircareViewModel(HaircareRepositoryImpl(database.haircareDao()))
    }
}
