package com.ego.mobile.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDomainTest {

    @Test
    fun `activity types cover all consolidated history sources`() {
        val activityType = Class.forName("com.ego.mobile.domain.model.ActivityType")
        val values = activityType.enumConstants.orEmpty().map { it.toString() }

        assertTrue(values.containsAll(listOf("HABIT", "TASK", "WORKOUT", "HAIRCARE")))
    }

    @Test
    fun `history item exposes the consolidated record details`() {
        assertProperties(
            "com.ego.mobile.domain.model.HistoryItem",
            "occurredAt",
            "type",
            "title",
            "subtitle",
            "status",
        )
    }

    @Test
    fun `home summary exposes today's activity and active routine`() {
        assertProperties(
            "com.ego.mobile.domain.model.HomeSummary",
            "todayHabits",
            "priorityTasks",
            "activeRoutine",
        )
        assertProperties(
            "com.ego.mobile.domain.model.HomeSummary\$TodayHabit",
            "habitId",
            "title",
            "status",
        )
        assertProperties(
            "com.ego.mobile.domain.model.HomeSummary\$ActiveRoutine",
            "id",
            "type",
            "title",
            "subtitle",
        )
    }

    private fun assertProperties(className: String, vararg expected: String) {
        val properties = Class.forName(className).declaredFields.map { it.name }
        assertTrue("$className must expose $expected", properties.containsAll(expected.toList()))
    }
}
