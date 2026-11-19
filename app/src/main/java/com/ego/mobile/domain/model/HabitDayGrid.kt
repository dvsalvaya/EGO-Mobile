package com.ego.mobile.domain.model

import java.time.LocalDate

/**
 * One slot in the seven-day habit grid shown on the Home screen.
 *
 * [predicted] – number of habits that were expected to be completed on this date,
 *              derived from each habit's plan effective on that date and from the
 *              weekly-goal progress up to that point.
 *              For [HabitFrequency.TimesPerWeek] with no preferred days, this is
 *              always 0 (metas semanais sem dias marcados não geram previsão diária).
 *              After a weekly goal is reached, later preferred days also contribute 0.
 *
 * [completed] – number of habits with a COMPLETED occurrence on this date.
 *
 * [isToday]  – true only for the current civil date in America/Sao_Paulo.
 */
enum class HabitGridBand {
    NO_PREDICTION,
    IN_PROGRESS,
    MISSED,
    LOW,
    PARTIAL,
    COMPLETE,
}

data class HabitGridDay(
    val date: LocalDate,
    val predicted: Int,
    val completed: Int,
    val isToday: Boolean = false,
) {
    init {
        require(predicted >= 0) { "Predicted count cannot be negative." }
        require(completed >= 0) { "Completed count cannot be negative." }
    }

    /** Null when no habits were expected; otherwise a ratio capped at 100%. */
    val completionRate: Double?
        get() = predicted.takeIf { it > 0 }?.let { (completed.toDouble() / it).coerceIn(0.0, 1.0) }

    /** Today remains open while an expected habit is outstanding. */
    val isInProgress: Boolean
        get() = isToday && predicted > completed

    /** Semantic range for the V1 grid; TASK-05 maps these to the approved colors. */
    val band: HabitGridBand
        get() {
            if (predicted == 0) return HabitGridBand.NO_PREDICTION
            if (isToday && completed == 0) return HabitGridBand.IN_PROGRESS
            if (completed == 0) return HabitGridBand.MISSED
            val rate = completionRate ?: return HabitGridBand.NO_PREDICTION
            return when {
                rate < 0.5 -> HabitGridBand.LOW
                rate < 1.0 -> HabitGridBand.PARTIAL
                else -> HabitGridBand.COMPLETE
            }
        }
}

/**
 * Seven-day habit grid, oldest day first, ending today.
 *
 * Used by the Home screen grade and by the consistency calculation of TASK-04.
 */
data class HabitDayGrid(
    val days: List<HabitGridDay>,
) {
    companion object {
        const val DAY_COUNT = 7

        /** Empty grid anchored to [today]. */
        fun empty(today: LocalDate) = HabitDayGrid(
            days = (DAY_COUNT - 1 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                HabitGridDay(date = date, predicted = 0, completed = 0, isToday = offset == 0)
            },
        )
    }
}
