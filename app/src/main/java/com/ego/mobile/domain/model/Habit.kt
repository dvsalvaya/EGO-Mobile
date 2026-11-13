package com.ego.mobile.domain.model

enum class HabitStatus {
    ACTIVE,
    PAUSED,
    ARCHIVED
}

data class Habit(
    val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val frequency: HabitFrequency,
    val status: HabitStatus = HabitStatus.ACTIVE,
    val createdAt: Long,
    val updatedAt: Long
) {
    init {
        require(name.isNotBlank()) { "Habit name is required." }
        require(name.trim().length in 1..80) { "Habit name must be 1 to 80 characters." }
        require(description == null || description.length <= 250) {
            "Habit description must be 250 characters or less."
        }
    }
}
