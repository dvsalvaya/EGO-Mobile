package com.ego.mobile.domain

import com.ego.mobile.domain.model.HabitGridBand
import com.ego.mobile.domain.model.HabitGridDay
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitGridDayTest {
    private val date = LocalDate.of(2026, 11, 18)

    @Test
    fun noPredictionHasNoRatioAndUsesEmptyBand() {
        val day = HabitGridDay(date, predicted = 0, completed = 0)

        assertNull(day.completionRate)
        assertEquals(HabitGridBand.NO_PREDICTION, day.band)
        assertFalse(day.isInProgress)
    }

    @Test
    fun zeroPercentOnClosedDayIsMissed() {
        val day = HabitGridDay(date, predicted = 3, completed = 0)

        assertEquals(0.0, day.completionRate!!, 0.0)
        assertEquals(HabitGridBand.MISSED, day.band)
    }

    @Test
    fun zeroPercentTodayWithOpenPredictionIsInProgressInsteadOfMissed() {
        val day = HabitGridDay(date, predicted = 2, completed = 0, isToday = true)

        assertTrue(day.isInProgress)
        assertEquals(HabitGridBand.IN_PROGRESS, day.band)
    }

    @Test
    fun partialUnderHalfUsesLowBand() {
        val day = HabitGridDay(date, predicted = 3, completed = 1)

        assertEquals(1.0 / 3.0, day.completionRate!!, 0.00001)
        assertEquals(HabitGridBand.LOW, day.band)
    }

    @Test
    fun exactlyHalfUsesPartialBand() {
        val day = HabitGridDay(date, predicted = 4, completed = 2)

        assertEquals(0.5, day.completionRate!!, 0.0)
        assertEquals(HabitGridBand.PARTIAL, day.band)
    }

    @Test
    fun oneHundredPercentUsesCompleteBandAndRateIsCapped() {
        val complete = HabitGridDay(date, predicted = 2, completed = 2)
        val factualExtra = HabitGridDay(date, predicted = 2, completed = 3)

        assertEquals(1.0, complete.completionRate!!, 0.0)
        assertEquals(HabitGridBand.COMPLETE, complete.band)
        assertEquals(1.0, factualExtra.completionRate!!, 0.0)
        assertEquals(HabitGridBand.COMPLETE, factualExtra.band)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeCountsAreRejected() {
        HabitGridDay(date, predicted = -1, completed = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeCompletionCountsAreRejected() {
        HabitGridDay(date, predicted = 0, completed = -1)
    }
}
