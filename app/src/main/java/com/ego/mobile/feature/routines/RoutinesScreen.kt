package com.ego.mobile.feature.routines

import androidx.compose.runtime.Composable
import com.ego.mobile.app.navigation.AppDestination
import com.ego.mobile.feature.common.PlaceholderScreen

@Composable
fun RoutinesScreen() {
    PlaceholderScreen(
        title = AppDestination.ROUTINES.title,
        subtitle = "Área de rotinas e treinos em desenvolvimento.",
        helperText = "Os treinos e os cuidados capilares ficam nesta área do MVP em construção.",
    )
}
