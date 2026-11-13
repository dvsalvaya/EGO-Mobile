package com.ego.mobile.feature.routines

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class RoutinesScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun routinesDestinationOffersHaircareTab() {
        composeRule.setContent {
            MaterialTheme {
                RoutinesScreen()
            }
        }

        composeRule.onNodeWithText("Cuidados capilares").assertIsDisplayed()
        composeRule.onNodeWithTag("haircare_tab").performClick()
        composeRule.onNodeWithText("Começar com Cronograma Capilar Básico").assertIsDisplayed()
        composeRule.onNodeWithText("Criar rotina personalizada").assertIsDisplayed()
    }
}
