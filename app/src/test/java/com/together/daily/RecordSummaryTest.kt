package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class RecordSummaryTest {
    private val today=LocalDate.of(2026,10,9)
    @Test fun dailyPercentTruncatesAndCountsMissingRecords() {
        val goals=(1..3).map {Goal(it.toString(),"me","Goal",today)}
        assertEquals(33,daySuccessPercent(goals,listOf(Entry("1",today,true)),today,today))
    }
    @Test fun futureAndUnscheduledDatesHaveNoPercent() {
        val goals=listOf(Goal("1","me","Goal",today))
        assertNull(daySuccessPercent(goals,emptyList(),today.plusDays(1),today))
        assertNull(daySuccessPercent(goals,emptyList(),today.minusDays(1),today))
    }
    @Test fun countsOnlyDistinctScheduledCompletedDatesInPeriod() {
        val goal=Goal("1","me","Goal",today.minusDays(2),today)
        val entries=listOf(Entry("1",today,true),Entry("1",today,true),Entry("1",today.minusDays(1),false),Entry("1",today.plusDays(1),true),Entry("other",today,true))
        assertEquals(1,successCount(goal,entries,today.minusDays(6),today.plusDays(2),today))
    }
}
