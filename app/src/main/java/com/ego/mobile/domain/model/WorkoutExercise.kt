package com.ego.mobile.domain.model

data class WorkoutExercise(
    val id: Long = 0L,
    val sessionId: Long = 0L,
    val name: String,
    val orderIndex: Int = 0,
    val setsCount: Int,
    val reps: String? = null,
    val weightKg: Double? = null,
    val notes: String? = null,
) {
    init {
        require(name.isNotBlank()) { "Exercise name must not be blank" }
        require(name.length <= 80) { "Exercise name must not exceed 80 characters" }
        require(setsCount > 0) { "Exercise sets count must be greater than zero" }
        if (weightKg != null) {
            require(weightKg >= 0.0) { "Exercise weight must not be negative" }
        }
    }
}
