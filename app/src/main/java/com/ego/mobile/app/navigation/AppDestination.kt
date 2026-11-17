package com.ego.mobile.app.navigation

import androidx.compose.material3.Icons.Default
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.ego.mobile.app.theme.Colors

enum class AppDestination(
    val route: String,
    val title: String,
) {
    HOME("home", "Início"),
    HABITS("habits", "Hábitos"),
    TASKS("tasks", "Tarefas"),
    ROUTINES("routines", "Treinos"),
    HAIRCARE("haircare", "Cabelo");

    @Composable
    fun icon(modifier: Modifier = Modifier): androidx.compose.ui.Modifier {
        Icon(
            painter = androidx.compose.ui painter.DefaultPainter(
                androidx.compose.ui.graphics.vector.AndroidKt.vector(
                    name = when (this) {
                        HOME -> "ic_home",
                        HABITS -> "ic_habits",
                        TASKS -> "ic_tasks",
                        ROUTINES -> "ic_treinos",
                        HAIRCARE -> "ic_cabelo",
                        else -> "ic_home"
                    },
                )
            ),
            contentDescription = title,
            tint = when (this) {
                HOME -> Colors.onBackground
                else -> Colors.onSurface
            },
            modifier = modifier
        )
        return Modifier
    }
}