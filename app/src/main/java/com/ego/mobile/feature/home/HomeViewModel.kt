package com.ego.mobile.feature.home

import android.database.SQLException
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
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.model.HomeSummary
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.usecase.GetHomeSummaryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(
    private val getHomeSummaryUseCase: GetHomeSummaryUseCase,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = eventChannel.receiveAsFlow()

    init {
        getHomeSummaryUseCase()
            .onStart { _uiState.value = HomeUiState.Loading }
            .onEach { summary -> _uiState.value = HomeUiState.Content(summary) }
            .catch { error ->
                _uiState.value = HomeUiState.Error(
                    error.message ?: "Não foi possível carregar o resumo.",
                )
            }
            .launchIn(viewModelScope)
    }

    fun onCompleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                habitRepository.completeOccurrence(habitId, today().toString())
                    .onFailure { error ->
                        setError(error.message ?: "Não foi possível concluir o hábito.")
                    }
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível concluir o hábito.")
            }
        }
    }

    fun onUndoHabit(habitId: Long) {
        viewModelScope.launch {
            habitRepository.undoOccurrence(habitId, today().toString()).onFailure { error ->
                setError(error.message ?: "Não foi possível desfazer a conclusão.")
            }
        }
    }

    fun onCompleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                taskRepository.completeTask(taskId, clock())
                    .onFailure { error ->
                        setError(error.message ?: "Não foi possível concluir a tarefa.")
                    }
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível concluir a tarefa.")
            }
        }
    }

    fun onCreateHabit(name: String, description: String?, frequency: HabitFrequency) {
        viewModelScope.launch {
            val now = clock()
            val habit = try {
                Habit(
                    name = name.trim(),
                    description = description?.trim()?.takeIf(String::isNotEmpty),
                    frequency = frequency,
                    status = HabitStatus.ACTIVE,
                    createdAt = now,
                    updatedAt = now,
                )
            } catch (error: IllegalArgumentException) {
                setError(error.message ?: "Verifique os dados do hábito.")
                return@launch
            }
            try {
                habitRepository.saveHabit(habit)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível salvar o hábito.")
            }
        }
    }

    fun onCreateTask(title: String, description: String?, dueAt: Long?) {
        viewModelScope.launch {
            val now = clock()
            val task = try {
                Task(
                    title = title.trim(),
                    description = description?.trim()?.takeIf(String::isNotEmpty),
                    dueAt = dueAt,
                    createdAt = now,
                    updatedAt = now,
                )
            } catch (error: IllegalArgumentException) {
                setError(error.message ?: "Verifique os dados da tarefa.")
                return@launch
            }
            try {
                taskRepository.createTask(task)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível salvar a tarefa.")
            }
        }
    }

    fun onCreateHabitRequested() {
        viewModelScope.launch { eventChannel.send(HomeEvent.OpenHabitCreation) }
    }

    fun onCreateTaskRequested() {
        viewModelScope.launch { eventChannel.send(HomeEvent.OpenTaskCreation) }
    }

    fun clearError() {
        val state = _uiState.value
        if (state is HomeUiState.Content) {
            _uiState.value = state.copy(errorMessage = null)
        }
    }

    private fun setError(message: String) {
        _uiState.value = when (val state = _uiState.value) {
            is HomeUiState.Content -> state.copy(errorMessage = message)
            HomeUiState.Loading,
            is HomeUiState.Error -> HomeUiState.Error(message)
        }
    }
}

sealed interface HomeEvent {
    data object OpenHabitCreation : HomeEvent
    data object OpenTaskCreation : HomeEvent
}

@Composable
fun rememberHomeViewModel(): HomeViewModel {
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
        HomeViewModel(
            getHomeSummaryUseCase = GetHomeSummaryUseCase(
                habitRepository = habitRepository,
                taskRepository = taskRepository,
                workoutRepository = workoutRepository,
                haircareRepository = haircareRepository,
            ),
            habitRepository = habitRepository,
            taskRepository = taskRepository,
        )
    }
}
