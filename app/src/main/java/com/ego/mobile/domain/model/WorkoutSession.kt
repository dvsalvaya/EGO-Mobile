package com.ego.mobile.domain.model

data class WorkoutSession(
    val id: Long = 0L,
    val title: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val status: WorkoutSessionStatus = WorkoutSessionStatus.IN_PROGRESS,
    val notes: String? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
) {
    init {
        require(title.isNotBlank()) { "Workout session title must not be blank" }
        require(startedAt > 0L) { "Workout session startedAt must be greater than zero" }
        if (endedAt != null) {
            require(endedAt >= startedAt) { "Workout session endedAt must be greater than or equal to startedAt" }
        }
    }

    fun canTransitionTo(newStatus: WorkoutSessionStatus): Boolean {
        if (this.status.isTerminal) {
            return false
        }
        return true
    }

    fun transitionTo(newStatus: WorkoutSessionStatus, timestamp: Long? = null): WorkoutSession {
        check(canTransitionTo(newStatus)) {
            "Cannot transition workout session from ${this.status} to $newStatus"
        }
        return copy(
            status = newStatus,
            endedAt = if (newStatus.isTerminal) (timestamp ?: System.currentTimeMillis()) else endedAt,
        )
    }

    fun durationMillis(now: Long = System.currentTimeMillis()): Long {
        return if (endedAt != null) {
            maxOf(0L, endedAt - startedAt)
        } else {
            maxOf(0L, now - startedAt)
        }
    }
}
