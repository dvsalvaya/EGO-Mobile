package com.ego.mobile.domain.model

enum class WorkoutSessionStatus {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELLED;

    val isActive: Boolean
        get() = this == IN_PROGRESS || this == PAUSED

    val isTerminal: Boolean
        get() = this == COMPLETED || this == CANCELLED
}
