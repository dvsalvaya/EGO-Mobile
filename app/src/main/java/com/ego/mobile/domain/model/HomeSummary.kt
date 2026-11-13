package com.ego.mobile.domain.model

data class HomeSummary(
    val todayHabits: List<TodayHabit>,
    val priorityTasks: List<Task>,
    val activeRoutine: ActiveRoutine?,
) {
    data class TodayHabit(
        val habitId: Long,
        val title: String,
        val status: HabitOccurrenceStatus,
    )

    data class ActiveRoutine(
        val id: Long,
        val type: ActivityType,
        val title: String,
        val subtitle: String?,
    )
}
