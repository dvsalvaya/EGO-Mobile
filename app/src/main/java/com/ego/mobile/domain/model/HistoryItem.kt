package com.ego.mobile.domain.model

import java.time.LocalDate

data class HabitPeriod(
    val habitId: Long,
    val start: LocalDate,
    val weeklyTarget: Int?,
    val days: List<HabitDay>,
    val today: LocalDate,
)

data class HistoryItem(
    val occurredAt: Long,
    val type: ActivityType,
    val title: String,
    val subtitle: String?,
    val status: String,
    val habitPeriod: HabitPeriod? = null,
)
