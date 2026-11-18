package com.ego.mobile.domain.model

data class WorkoutSession(
    val id: Long = 0L,
    val title: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val status: WorkoutSessionStatus = WorkoutSessionStatus.IN_PROGRESS,
    val notes: String? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    val pausedAt: Long? = null,
    val pausedDurationMillis: Long = 0L,
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
        val transitionTime = timestamp ?: System.currentTimeMillis()
        val resumedPause = if (status == WorkoutSessionStatus.PAUSED && newStatus == WorkoutSessionStatus.IN_PROGRESS) {
            maxOf(0L, transitionTime - (pausedAt ?: transitionTime))
        } else 0L
        val terminalPause = if (status == WorkoutSessionStatus.PAUSED && newStatus.isTerminal) {
            maxOf(0L, transitionTime - (pausedAt ?: transitionTime))
        } else 0L
        return copy(
            status = newStatus,
            endedAt = if (newStatus.isTerminal) transitionTime else endedAt,
            pausedAt = if (newStatus == WorkoutSessionStatus.PAUSED) transitionTime else null,
            pausedDurationMillis = pausedDurationMillis + resumedPause + terminalPause,
        )
    }

    fun durationMillis(now: Long = System.currentTimeMillis()): Long {
        val endTime = endedAt ?: now
        val currentPause = if (status == WorkoutSessionStatus.PAUSED) maxOf(0L, endTime - (pausedAt ?: endTime)) else 0L
        return maxOf(0L, endTime - startedAt - pausedDurationMillis - currentPause)
    }
}
