package com.ego.mobile.feature.routines.haircare

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.HaircareRoutineSource
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.domain.repository.HaircareRepository
import java.time.Instant

class HaircareHistoryListTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsCompletedStepsWithExecutionTimeAndNotesInNewestFirstOrder() {
        val viewModel = HaircareViewModel(HistoryHaircareRepository())
        composeRule.setContent {
            MaterialTheme {
                HaircareScreen(viewModel)
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.uiState.value is HaircareUiState.ActiveRoutine
        }

        composeRule.onNodeWithTag("haircare_screen_list").performScrollToIndex(6)
        composeRule.onNodeWithText("Histórico de cuidados").assertIsDisplayed()
        composeRule.onNodeWithText("Nutrição concluída").assertIsDisplayed()
        composeRule.onNodeWithText("Reconstrução concluída").assertIsDisplayed()
        composeRule.onNodeWithText("Executada em 13/11/2026 às 07:00").assertIsDisplayed()
        composeRule.onNodeWithText("Notas: Máscara nutritiva").assertIsDisplayed()

        val newerY = composeRule.onNodeWithText("Reconstrução concluída")
            .fetchSemanticsNode().boundsInRoot.top
        val olderY = composeRule.onNodeWithText("Nutrição concluída")
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue("Newest completion should appear first", newerY < olderY)
    }

    @Test
    fun calendarNavigatesMonthsAndSelectsDayWithOccurrence() {
        val viewModel = HaircareViewModel(HistoryHaircareRepository())
        composeRule.setContent {
            MaterialTheme {
                HaircareScreen(viewModel)
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.uiState.value is HaircareUiState.ActiveRoutine
        }
        composeRule.onNodeWithText("Calendário capilar").assertIsDisplayed()
        composeRule.onNodeWithText("Novembro 2026").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("12 de Novembro de 2026, 1 concluída").performClick()
        composeRule.onNodeWithText("Etapas de 12/11").assertIsDisplayed()
        composeRule.onNodeWithText("Nutrição concluída").assertIsDisplayed()

        val selectedDay = composeRule.onNodeWithContentDescription("12 de Novembro de 2026, 1 concluída")
            .fetchSemanticsNode().boundsInRoot
        val minimumTargetPx = 48f * composeRule.activity.resources.displayMetrics.density
        assertTrue("Calendar day target must be at least 48dp wide: $selectedDay", selectedDay.width >= minimumTargetPx)
        assertTrue("Calendar day target must be at least 48dp high: $selectedDay", selectedDay.height >= minimumTargetPx)

        composeRule.onNodeWithContentDescription("Próximo mês").performClick()
        composeRule.onNodeWithText("Dezembro 2026").assertIsDisplayed()
    }
}

private class HistoryHaircareRepository : HaircareRepository {
    private val routine = HaircareRoutine(
        id = 1L,
        name = "Cronograma de teste",
        source = HaircareRoutineSource.CUSTOM,
        createdAt = 1L,
        updatedAt = 1L,
    )
    private val pendingStep = HaircareOccurrence(
        id = 3L,
        routineId = routine.id,
        stepType = HaircareStepType.HIDRATACAO,
        stepTitle = "Hidratação pendente",
        scheduledDate = "2026-11-14",
    )
    private val completedSteps = listOf(
        HaircareOccurrence(
            id = 1L,
            routineId = routine.id,
            stepType = HaircareStepType.NUTRICAO,
            stepTitle = "Nutrição concluída",
            scheduledDate = "2026-11-12",
            executedAt = Instant.parse("2026-11-12T10:00:00Z").toEpochMilli(),
            status = HaircareOccurrenceStatus.COMPLETED,
            notes = "Máscara nutritiva",
        ),
        HaircareOccurrence(
            id = 2L,
            routineId = routine.id,
            stepType = HaircareStepType.RECONSTRUCAO,
            stepTitle = "Reconstrução concluída",
            scheduledDate = "2026-11-13",
            executedAt = Instant.parse("2026-11-13T10:00:00Z").toEpochMilli(),
            status = HaircareOccurrenceStatus.COMPLETED,
        ),
    )

    override fun observeActiveRoutine(): Flow<HaircareRoutine?> = flowOf(routine)

    override fun observeOccurrencesForRoutine(routineId: Long): Flow<List<HaircareOccurrence>> =
        flowOf(listOf(pendingStep))

    override fun observeNextPendingOccurrence(routineId: Long): Flow<HaircareOccurrence?> =
        flowOf(pendingStep)

    override fun observeCompletedOccurrences(): Flow<List<HaircareOccurrence>> = flowOf(completedSteps)

    override suspend fun createRoutine(
        routine: HaircareRoutine,
        occurrences: List<HaircareOccurrence>,
    ): Result<Long> = Result.failure(UnsupportedOperationException())

    override suspend fun recordExecution(
        occurrenceId: Long,
        timestamp: Long,
        notes: String?,
    ): Result<Unit> = Result.failure(UnsupportedOperationException())

    override suspend fun rescheduleStep(occurrenceId: Long, newDateCivil: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}
