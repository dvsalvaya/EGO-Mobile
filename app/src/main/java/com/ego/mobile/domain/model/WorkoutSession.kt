package com.ego.mobile.domain.model

data class WorkoutSession(
    val id: Long = 0L,
    val title: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val status: WorkoutSessionStatus = WorkoutSessionStatus.IN_PROGRESS,
    val notes: String? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    /** Instante em que a pausa atual começou; nulo quando a sessão não está pausada. */
    val pausedAt: Long? = null,
    /** Soma de todos os intervalos pausados já concluídos. */
    val pausedDurationMillis: Long = 0L,
) {
    init {
        require(title.isNotBlank()) { "Workout session title must not be blank" }
        require(startedAt > 0L) { "Workout session startedAt must be greater than zero" }
        if (endedAt != null) {
            require(endedAt >= startedAt) { "Workout session endedAt must be greater than or equal to startedAt" }
        }
        if (pausedAt != null) {
            require(pausedAt >= startedAt) { "Workout session pausedAt must be greater than or equal to startedAt" }
        }
        require(pausedDurationMillis >= 0L) { "Workout session pausedDurationMillis must not be negative" }
    }

    fun canTransitionTo(newStatus: WorkoutSessionStatus): Boolean = !status.isTerminal

    fun transitionTo(newStatus: WorkoutSessionStatus, timestamp: Long? = null): WorkoutSession {
        check(canTransitionTo(newStatus)) {
            "Cannot transition workout session from ${this.status} to $newStatus"
        }
        val at = timestamp ?: System.currentTimeMillis()
        val accumulatedPause = if (status == WorkoutSessionStatus.PAUSED && pausedAt != null) {
            pausedDurationMillis + maxOf(0L, at - pausedAt!!)
        } else {
            pausedDurationMillis
        }
        return copy(
            status = newStatus,
            endedAt = if (newStatus.isTerminal) at else endedAt,
            pausedAt = if (newStatus == WorkoutSessionStatus.PAUSED) at else null,
            pausedDurationMillis = accumulatedPause,
        )
    }

    /** Duração efetiva, sem os intervalos em que a sessão esteve pausada. */
    fun durationMillis(now: Long = System.currentTimeMillis()): Long {
        val reference = when {
            status == WorkoutSessionStatus.PAUSED && pausedAt != null -> pausedAt!!
            endedAt != null -> endedAt!!
            else -> now
        }
        return maxOf(0L, reference - startedAt - pausedDurationMillis)
    }
}
