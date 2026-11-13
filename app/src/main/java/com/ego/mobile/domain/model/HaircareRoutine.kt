package com.ego.mobile.domain.model

enum class HaircareRoutineSource {
    PRESET,
    CUSTOM,
}

data class HaircareRoutine(
    val id: Long = 0L,
    val name: String,
    val source: HaircareRoutineSource = HaircareRoutineSource.CUSTOM,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    init {
        require(name.isNotBlank()) { "Routine name must not be blank" }
        require(name.trim().length in 1..80) { "Routine name must be 1 to 80 characters" }
        require(description == null || description.length <= 250) { "Description must be 250 characters or less" }
    }
}
