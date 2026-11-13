package com.ego.mobile.feature.tasks

import android.database.SQLException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ego.mobile.data.local.EgoDatabaseProvider
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.usecase.DeleteTaskWithUndoUseCase
import com.ego.mobile.domain.usecase.RescheduleTaskUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TaskRepository,
    private val deleteTaskWithUndoUseCase: DeleteTaskWithUndoUseCase =
        DeleteTaskWithUndoUseCase(repository),
    private val rescheduleTaskUseCase: RescheduleTaskUseCase = RescheduleTaskUseCase(),
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TasksUiState())
    val tasksUiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<TasksEvent>(Channel.BUFFERED)
    val events: Flow<TasksEvent> = eventChannel.receiveAsFlow()

    init {
        combine(
            repository.getOpenTasks(),
            repository.getCompletedTasks(),
            clockFlow(),
        ) { openTasks, completedTasks, now ->
            _uiState.value.copy(
                openTasks = openTasks.sortedWith(
                    compareBy<Task>({ !it.isOverdue(now) }, { it.dueAt ?: Long.MAX_VALUE }, { -it.createdAt }),
                ),
                completedTasks = completedTasks,
                isLoading = false,
                errorMessage = null,
                currentTimeMillis = now,
            )
        }
            .onStart { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Não foi possível carregar as tarefas.",
                )
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
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
                repository.createTask(task)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível salvar a tarefa.")
            }
        }
    }

    fun onUpdateTask(task: Task) {
        viewModelScope.launch {
            val updated = try {
                task.copy(
                    title = task.title.trim(),
                    description = task.description?.trim()?.takeIf(String::isNotEmpty),
                    updatedAt = clock(),
                )
            } catch (error: IllegalArgumentException) {
                setError(error.message ?: "Verifique os dados da tarefa.")
                return@launch
            }
            try {
                repository.updateTask(updated)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível atualizar a tarefa.")
            }
        }
    }

    fun onCompleteTask(taskId: Long) {
        viewModelScope.launch {
            val result = repository.completeTask(taskId, clock())
            if (result.isFailure) {
                setError(result.exceptionOrNull()?.message ?: "Não foi possível concluir a tarefa.")
            }
        }
    }

    fun onRescheduleTask(taskId: Long, newDueAt: Long) {
        viewModelScope.launch {
            val task = try {
                repository.getTaskById(taskId)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível carregar a tarefa.")
                return@launch
            }
            if (task == null) {
                setError("Tarefa não encontrada.")
                return@launch
            }
            val updated = try {
                rescheduleTaskUseCase(task, newDueAt)
            } catch (error: IllegalArgumentException) {
                setError(error.message ?: "Prazo inválido.")
                return@launch
            }
            try {
                repository.updateTask(updated)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível reagendar a tarefa.")
            }
        }
    }

    fun onDeleteTask(taskId: Long) {
        viewModelScope.launch {
            val result = deleteTaskWithUndoUseCase(taskId)
            if (result.isSuccess) {
                eventChannel.send(TasksEvent.TaskDeleted(result.getOrThrow().deletedTask.title))
            } else {
                setError(result.exceptionOrNull()?.message ?: "Não foi possível excluir a tarefa.")
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            val result = deleteTaskWithUndoUseCase.undo()
            if (result.isFailure) {
                setError(result.exceptionOrNull()?.message ?: "Não foi possível desfazer a exclusão.")
            }
        }
    }

    fun discardPendingUndo() {
        deleteTaskWithUndoUseCase.discardPendingUndo()
    }

    fun setShowCompletedTasks(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCompletedTasks = show)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun clockFlow(): Flow<Long> = flow {
        while (true) {
            emit(clock())
            delay(CLOCK_REFRESH_MILLIS)
        }
    }

    private fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    private companion object {
        const val CLOCK_REFRESH_MILLIS = 60_000L
    }
}

@Composable
fun rememberTasksViewModel(): TasksViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val database = EgoDatabaseProvider.getDatabase(context)
        TasksViewModel(TaskRepositoryImpl(database.taskDao()))
    }
}
