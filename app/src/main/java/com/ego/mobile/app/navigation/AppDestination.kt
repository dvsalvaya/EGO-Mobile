package com.ego.mobile.app.navigation

enum class AppDestination(
    val route: String,
    val title: String,
    val iconGlyph: String,
) {
    HOME("home", "Início", "⌂"),
    HABITS("habits", "Hábitos", "✓"),
    TASKS("tasks", "Tarefas", "☑"),
    ROUTINES("routines", "Rotinas", "◫"),
    HISTORY("history", "Histórico", "◌");
}
