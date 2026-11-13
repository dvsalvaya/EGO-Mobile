package com.ego.mobile.feature.tasks

import androidx.compose.runtime.Composable
import com.ego.mobile.app.navigation.AppDestination
import com.ego.mobile.feature.common.PlaceholderScreen

@Composable
fun TasksScreen() {
    PlaceholderScreen(
        title = AppDestination.TASKS.title,
        subtitle = "Área de tarefas em desenvolvimento.",
        helperText = "A lista de tarefas abertas, prazos e estados de conclusão será exibida aqui.",
    )
}
