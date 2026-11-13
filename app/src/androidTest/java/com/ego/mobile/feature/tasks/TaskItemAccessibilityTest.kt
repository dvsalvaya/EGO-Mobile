package com.ego.mobile.feature.tasks

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ego.mobile.domain.model.Task
import com.ego.mobile.feature.tasks.components.TaskItem
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskItemAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun completionCheckboxHasTaskSpecificAccessibleNameAndMinimumTouchTarget() {
        var completed = false
        composeRule.setContent {
            MaterialTheme {
                TaskItem(
                    task = Task(
                        id = 1L,
                        title = "Revisar proposta",
                        createdAt = 1L,
                        updatedAt = 1L,
                    ),
                    nowMillis = 1L,
                    onComplete = { completed = true },
                    onEdit = {},
                    onReschedule = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("Revisar proposta").assertIsDisplayed()
        val checkbox = composeRule.onNodeWithContentDescription("Concluir tarefa: Revisar proposta")
        checkbox.assertIsDisplayed()
        val minimumTargetPx = with(composeRule.density) { 48.dp.toPx() }
        val bounds = checkbox.fetchSemanticsNode().boundsInRoot
        assertTrue("Checkbox touch target width must be at least 48dp", bounds.width >= minimumTargetPx)
        assertTrue("Checkbox touch target height must be at least 48dp", bounds.height >= minimumTargetPx)
        checkbox.performClick()

        val editAction = composeRule.onNodeWithText("Editar")
        val editBounds = editAction.fetchSemanticsNode().boundsInRoot
        assertTrue("Text action touch target width must be at least 48dp", editBounds.width >= minimumTargetPx)
        assertTrue("Text action touch target height must be at least 48dp", editBounds.height >= minimumTargetPx)
        composeRule.runOnIdle { assertTrue(completed) }
    }
}
