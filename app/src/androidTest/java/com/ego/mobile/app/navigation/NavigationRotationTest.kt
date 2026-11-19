package com.ego.mobile.app.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ego.mobile.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationRotationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun historyRouteSurvivesActivityRecreationAndBackReturnsToHome() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Ver histórico").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Ver histórico").performClick()
        composeRule.onNodeWithText("Histórico").assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Histórico").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithText("Ver histórico").assertIsDisplayed()
    }
}
