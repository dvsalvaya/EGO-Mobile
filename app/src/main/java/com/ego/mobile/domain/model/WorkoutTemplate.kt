package com.ego.mobile.domain.model

/** Modelo de treino reutilizável, distinto de uma sessão executada. */
data class WorkoutTemplate(
    val id: Long = 0L,
    val name: String,
    val exercises: List<WorkoutTemplateExercise> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    init {
        require(name.isNotBlank()) { "Workout template name must not be blank" }
        require(name.trim().length <= 80) { "Workout template name must not exceed 80 characters" }
    }

    val orderedExercises: List<WorkoutTemplateExercise>
        get() = exercises.sortedBy { it.orderIndex }
}

data class WorkoutTemplateExercise(
    val id: Long = 0L,
    val templateId: Long = 0L,
    val name: String,
    val orderIndex: Int,
    val setsCount: Int,
    val reps: String? = null,
    val weightKg: Double? = null,
    val notes: String? = null,
) {
    init {
        require(name.isNotBlank()) { "Template exercise name must not be blank" }
        require(name.trim().length <= 80) { "Template exercise name must not exceed 80 characters" }
        require(orderIndex >= 0) { "Template exercise orderIndex must not be negative" }
        require(setsCount > 0) { "Template exercise sets count must be greater than zero" }
        require(weightKg == null || weightKg >= 0.0) { "Template exercise weight must not be negative" }
    }
}
