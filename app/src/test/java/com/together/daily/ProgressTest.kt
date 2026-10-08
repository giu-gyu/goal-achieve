package com.together.daily

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ProgressTest {
    private fun d(s: String) = LocalDate.parse(s)
    @Test fun creationAndFutureDaysDoNotDiluteRate() {
        val g = Goal("g", "u", "걷기", d("2026-10-07"))
        val p = progress(g, listOf(Entry("g", d("2026-10-07"), true), Entry("g", d("2026-10-08"), false)), d("2026-10-05"), d("2026-10-11"), d("2026-10-09"))
        assertEquals(Progress(1, 1, 1), p)
        assertEquals(33, p.percent)
    }
    @Test fun archivedGoalsKeepHistoricalDenominator() {
        val g = Goal("g", "u", "걷기", d("2026-09-28"), d("2026-10-02"))
        assertEquals(Progress(0, 0, 2), progress(g, emptyList(), d("2026-10-01"), d("2026-10-31"), d("2026-10-09")))
    }
    @Test fun noEligibleDaysAndOtherGoals() {
        val g = Goal("g", "u", "걷기", d("2026-10-10"))
        assertEquals(0, progress(g, emptyList(), d("2026-10-01"), d("2026-10-31"), d("2026-10-09")).total)
        assertEquals(Progress(0, 0, 1), progress(g, listOf(Entry("other", d("2026-10-10"), true)), d("2026-10-10"), d("2026-10-10"), d("2026-10-10")))
    }
    @Test fun weekCrossesYearAndMonthBoundaries() {
        assertEquals(d("2025-12-29"), weekStart(d("2026-01-01")))
    }
}
