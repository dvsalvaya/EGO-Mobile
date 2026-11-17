package com.ego.mobile.feature.routines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.ego.mobile.feature.routines.haircare.HaircareScreen
import com.ego.mobile.feature.routines.workouts.WorkoutScreen
import com.ego.mobile.app.navigation.AppDestination

@Composable
fun RoutinesScreen(initialSelectedTab: Int = 0) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialSelectedTab.coerceIn(0, 1)) }

    LaunchedEffect(initialSelectedTab) {
        selectedTab = initialSelectedTab.coerceIn(0, 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            listOf("Treinos", "Cuidados capilares").forEachIndexed { index, title ->
                Tab(
                    modifier = androidx.compose.ui.platform.testTagIfSpec(if (index == 1) "haircare_tab" else "workouts_tab"),
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> WorkoutScreen()
                else -> HaircareScreen()
            }
        }
    }
}