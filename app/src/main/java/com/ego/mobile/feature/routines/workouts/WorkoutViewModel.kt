package com.ego.mobile.feature.routines.workouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ego.mobile.data.local.EgoDatabaseProvider
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val repository: WorkoutRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState(isLoading = true))
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<WorkoutEvent>(Channel.BUFFERED)
    val events: Flow<WorkoutEvent> = eventChannel.receiveAsFlow()

    init {
        combine(
            repository.getActiveSession(),
            repository.getCompletedSessions(),
            timerFlow(),
        ) { active, completed, now ->
            val elapsed = active?.durationMillis(now) ?: 0L
            _uiState.value.copy(
                activeSession = active,
                completedSessions = completed,
                elapsedTimeMillis = elapsed,
                isLoading = false,
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun startSession(title: String) {
        viewModelScope.launch {
            val sessionTitle = title.ifBlank {
                "Treino"
            }
            repository.startSession(sessionTitle, clock())
                .onSuccess {
                    eventChannel.send(WorkoutEvent.SessionStarted)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                    eventChannel.send(WorkoutEvent.Error(error.message ?: "Erro ao iniciar treino"))
                }
        }
    }

    fun pauseSession() {
        val currentId = _uiState.value.activeSession?.id ?: return
        viewModelScope.launch {
            repository.pauseSession(currentId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun resumeSession() {
        val currentId = _uiState.value.activeSession?.id ?: return
        viewModelScope.launch {
            repository.resumeSession(currentId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun completeSession() {
        val currentId = _uiState.value.activeSession?.id ?: return
        viewModelScope.launch {
            repository.completeSession(currentId, clock())
                .onSuccess {
                    eventChannel.send(WorkoutEvent.SessionCompleted)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                    eventChannel.send(WorkoutEvent.Error(error.message ?: "Erro ao finalizar treino"))
                }
        }
    }

    fun discardSession() {
        val currentId = _uiState.value.activeSession?.id ?: return
        viewModelScope.launch {
            repository.deleteSession(currentId)
                .onSuccess {
                    eventChannel.send(WorkoutEvent.SessionDiscarded)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun addExercise(
        name: String,
        setsCount: Int,
        reps: String? = null,
        weightKg: Double? = null,
        notes: String? = null,
    ) {
        val current = _uiState.value.activeSession ?: return
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nome do exercício é obrigatório")
            return
        }
        if (setsCount <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Quantidade de séries deve ser maior que zero")
            return
        }

        viewModelScope.launch {
            val nextOrderIndex = current.exercises.size
            val exercise = WorkoutExercise(
                sessionId = current.id,
                name = name.trim(),
                orderIndex = nextOrderIndex,
                setsCount = setsCount,
                reps = reps?.trim()?.ifBlank { null },
                weightKg = weightKg,
                notes = notes?.trim()?.ifBlank { null },
            )

            repository.addExercise(exercise)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun removeExercise(exerciseId: Long) {
        viewModelScope.launch {
            repository.removeExercise(exerciseId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun timerFlow(): Flow<Long> = flow {
        while (true) {
            emit(clock())
            delay(TIMER_INTERVAL_MILLIS)
        }
    }

    private companion object {
        const val TIMER_INTERVAL_MILLIS = 1000L
    }
}

@Composable
fun rememberWorkoutViewModel(): WorkoutViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val database = EgoDatabaseProvider.getDatabase(context)
        WorkoutViewModel(WorkoutRepositoryImpl(database.workoutDao()))
    }
}
