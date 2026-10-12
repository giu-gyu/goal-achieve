package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CelebrationTest {
    private val day=LocalDate.parse("2026-11-02")
    private val goals=listOf(Goal("a","me","산책",day),Goal("b","me","공부",day))
    @Test fun mixedDoneAndMissedRecordsCompleteTheDay() {
        assertTrue(allGoalsRecorded(goals,listOf(Entry("a",day,true),Entry("b",day,false)),day))
    }
    @Test fun missingOtherDateOrOtherGoalDoesNotCompleteTheDay() {
        assertFalse(allGoalsRecorded(goals,listOf(Entry("a",day,true),Entry("b",day.minusDays(1),false)),day))
        assertFalse(allGoalsRecorded(goals,listOf(Entry("a",day,true),Entry("other",day,true)),day))
        assertFalse(allGoalsRecorded(emptyList(),emptyList(),day))
    }
}
