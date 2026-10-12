package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DateScheduleTest {
    private val monday=LocalDate.parse("2026-11-02")
    @Test fun calendarDefaultsAndOverridesExpireWithTheDate() {
        val overrides=mapOf(monday to GoalSchedule.WEEKEND)
        assertEquals(GoalSchedule.WEEKDAY,scheduleForDate(monday))
        assertEquals(GoalSchedule.WEEKEND,scheduleForDate(monday,overrides))
        assertEquals(GoalSchedule.WEEKDAY,scheduleForDate(monday.plusDays(1),overrides))
        assertEquals(GoalSchedule.WEEKEND,scheduleForDate(LocalDate.parse("2026-10-09")))
    }
    @Test fun changingDayMovesEligibilityAndProgressToTheOtherCategory() {
        val overrides=mapOf(monday to GoalSchedule.WEEKEND)
        val weekday=Goal("weekday","me","공부",monday,dateSchedules=overrides)
        val weekend=Goal("weekend","me","산책",monday,schedule=GoalSchedule.WEEKEND,dateSchedules=overrides)
        assertFalse(weekday.scheduled(monday))
        assertTrue(weekend.scheduled(monday))
        assertEquals(0,progress(weekday,emptyList(),monday,monday,monday).total)
        assertEquals(100,progress(weekend,listOf(Entry("weekend",monday,true)),monday,monday,monday).percent)
    }
}
