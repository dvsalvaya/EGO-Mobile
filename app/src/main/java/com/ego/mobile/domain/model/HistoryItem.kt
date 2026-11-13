package com.ego.mobile.domain.model

data class HistoryItem(
    val occurredAt: Long,
    val type: ActivityType,
    val title: String,
    val subtitle: String?,
    val status: String,
)
