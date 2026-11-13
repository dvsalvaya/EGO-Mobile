package com.ego.mobile.domain.model

enum class HaircareOccurrenceStatus {
    PENDING,
    COMPLETED,
    RESCHEDULED,
    SKIPPED,
}

data class HaircareOccurrence(
    val id: Long = 0L,
    val routineId: Long = 0L,
    val stepType: HaircareStepType,
    val stepTitle: String,
    val scheduledDate: String, // 'YYYY-MM-DD'
    val executedAt: Long? = null,
    val status: HaircareOccurrenceStatus = HaircareOccurrenceStatus.PENDING,
    val notes: String? = null,
    val orderIndex: Int = 0,
) {
    init {
        require(stepTitle.isNotBlank()) { "Step title must not be blank" }
        require(stepTitle.trim().length in 1..80) { "Step title must be 1 to 80 characters" }
        require(scheduledDate.matches(DATE_REGEX)) { "scheduledDate must be in format YYYY-MM-DD" }
        require(notes == null || notes.length <= 250) { "Notes must be 250 characters or less" }
    }

    fun complete(timestamp: Long = System.currentTimeMillis(), executionNotes: String? = notes): HaircareOccurrence {
        return copy(
            status = HaircareOccurrenceStatus.COMPLETED,
            executedAt = timestamp,
            notes = executionNotes,
        )
    }

    fun reschedule(newDate: String): HaircareOccurrence {
        require(newDate.matches(DATE_REGEX)) { "newDate must be in format YYYY-MM-DD" }
        return copy(
            scheduledDate = newDate,
            status = HaircareOccurrenceStatus.RESCHEDULED,
        )
    }

    companion object {
        private val DATE_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")
    }
}
