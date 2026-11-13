package com.ego.mobile.feature

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.data.repository.HaircareRepositoryImpl
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.usecase.GetConsolidatedHistoryUseCase
import com.ego.mobile.domain.usecase.GetHomeSummaryUseCase
import com.ego.mobile.feature.habits.HabitsViewModel
import com.ego.mobile.feature.history.HistoryViewModel
import com.ego.mobile.feature.home.HomeUiState
import com.ego.mobile.feature.home.HomeViewModel
import com.ego.mobile.feature.tasks.TasksViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HomeHistoryConsistencyTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: EgoDatabase
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModelStore = ViewModelStore()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun completingItemsFromHomeUpdatesHistoryAndFeatureLists() = runBlocking {
        val now = System.currentTimeMillis()
        val today = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
        val habitRepository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val taskRepository = TaskRepositoryImpl(database.taskDao())
        val workoutRepository = WorkoutRepositoryImpl(database.workoutDao())
        val haircareRepository = HaircareRepositoryImpl(database.haircareDao())

        val habitId = habitRepository.saveHabit(
            Habit(
                name = "Alongar",
                frequency = HabitFrequency.Daily,
                createdAt = now,
                updatedAt = now,
            ),
        )
        val taskId = taskRepository.createTask(
            Task(
                title = "Revisar agenda",
                createdAt = now,
                updatedAt = now,
            ),
        )

        val homeViewModel = HomeViewModel(
            getHomeSummaryUseCase = GetHomeSummaryUseCase(
                habitRepository = habitRepository,
                taskRepository = taskRepository,
                workoutRepository = workoutRepository,
                haircareRepository = haircareRepository,
                today = { today },
            ),
            habitRepository = habitRepository,
            taskRepository = taskRepository,
            clock = { now + 1_000L },
            today = { today },
        ).also { viewModelStore.put("home", it) }
        val historyViewModel = HistoryViewModel(
            GetConsolidatedHistoryUseCase(
                habitRepository = habitRepository,
                taskRepository = taskRepository,
                workoutRepository = workoutRepository,
                haircareRepository = haircareRepository,
            ),
        ).also { viewModelStore.put("history", it) }
        val habitsViewModel = HabitsViewModel(habitRepository)
            .also { viewModelStore.put("habits", it) }
        val tasksViewModel = TasksViewModel(
            repository = taskRepository,
            clock = { now + 2_000L },
        ).also { viewModelStore.put("tasks", it) }

        composeRule.waitUntil(10_000) {
            homeViewModel.uiState.value is HomeUiState.Content &&
                !historyViewModel.uiState.value.isLoading &&
                !habitsViewModel.uiState.value.isLoading &&
                !tasksViewModel.tasksUiState.value.isLoading &&
                habitsViewModel.uiState.value.activeHabits.any { it.id == habitId } &&
                tasksViewModel.tasksUiState.value.openTasks.any { it.id == taskId }
        }

        composeRule.runOnIdle {
            homeViewModel.onCompleteHabit(habitId)
            homeViewModel.onCompleteTask(taskId)
        }

        composeRule.waitUntil(10_000) {
            val homeState = homeViewModel.uiState.value as? HomeUiState.Content
            val historyItems = historyViewModel.uiState.value.items
            val tasksState = tasksViewModel.tasksUiState.value
            homeState?.summary?.todayHabits?.any {
                it.habitId == habitId && it.status == HabitOccurrenceStatus.COMPLETED
            } == true &&
                historyItems.any { it.title == "Alongar" } &&
                historyItems.any { it.title == "Revisar agenda" } &&
                tasksState.openTasks.none { it.id == taskId } &&
                tasksState.completedTasks.any { it.id == taskId && it.status == TaskStatus.COMPLETED }
        }

        tasksViewModel.setShowCompletedTasks(true)
        composeRule.waitUntil(5_000) {
            tasksViewModel.tasksUiState.value.visibleTasks.any {
                it.id == taskId && it.completedAt != null
            }
        }

        assertTrue(habitsViewModel.uiState.value.activeHabits.any { it.id == habitId })
        assertEquals(
            HabitOccurrenceStatus.COMPLETED,
            habitRepository.getOccurrenceForDate(habitId, today.toString())?.status,
        )
        assertEquals(
            setOf("Alongar", "Revisar agenda"),
            historyViewModel.uiState.value.items.map { it.title }.toSet(),
        )
        assertTrue(tasksViewModel.tasksUiState.value.visibleTasks.single().completedAt != null)
    }
}
