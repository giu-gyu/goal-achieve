package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ScheduleProgressTest {
    private fun d(s:String)=LocalDate.parse(s)
    private val start=d("2026-11-02")
    private fun goal(target:Int?=null,schedule:GoalSchedule=GoalSchedule.WEEKDAY)=Goal("g","me","걷기",start,schedule=schedule,weeklyTarget=target)
    private fun rate(g:Goal,records:List<Entry>,end:LocalDate=start.plusDays(6))=progress(g,records,start,end,end)
    @Test fun weekdayAndRestDayGoalsNeverOverlap() {
        val weekday=goal(); val weekend=goal(schedule=GoalSchedule.WEEKEND)
        for(offset in 0L..6L) {
            val day=start.plusDays(offset)
            assertNotEquals(weekday.scheduled(day),weekend.scheduled(day))
        }
        assertEquals(5,rate(weekday,emptyList()).total)
        assertEquals(2,rate(weekend,emptyList()).total)
    }
    @Test fun publicAndSubstituteHolidaysAreRestDays() {
        listOf("2026-10-09","2026-10-05","2026-02-17","2026-05-01","2026-07-17","2027-02-09","2027-07-19").forEach {
            assertTrue(KoreanHolidays.isRestDay(d(it)))
            assertFalse(GoalSchedule.WEEKDAY.includes(d(it)))
        }
        assertFalse(KoreanHolidays.isHoliday(d("2027-03-02")))
    }
    @Test fun twoPerWeekIsHalfThenFullAndNeverAboveFull() {
        val g=goal(2)
        val records=(0L..2L).map {Entry("g",start.plusDays(it),true)}
        assertEquals(50,rate(g,records.take(1)).percent)
        assertEquals(100,rate(g,records.take(2)).percent)
        assertEquals(100,rate(g,records).percent)
    }
    @Test fun threePerWeekCapsAtThreeAndDeduplicatesDates() {
        val g=goal(3)
        val records=(0L..3L).map {Entry("g",start.plusDays(it),true)}
        assertEquals(33,rate(g,listOf(records[0],records[0])).percent)
        assertEquals(100,rate(g,records).percent)
        assertEquals(3,rate(g,records).done)
    }
    @Test fun eachWeekHasItsOwnTargetAndExcessDoesNotCarry() {
        val g=goal(2)
        val records=(0L..3L).map {Entry("g",start.plusDays(it),true)}
        val p=rate(g,records,start.plusDays(13))
        assertEquals(4,p.total)
        assertEquals(50,p.percent)
    }
    @Test fun weeklyTargetAppliesBeforeTheWeekendAndFutureRecordsAreExcluded() {
        val g=goal(2,GoalSchedule.WEEKEND)
        assertEquals(2,progress(g,emptyList(),start,start.plusDays(6),start.plusDays(3)).total)
        assertEquals(0,progress(g,listOf(Entry("g",start.plusDays(5),true)),start,start.plusDays(6),start.plusDays(3)).done)
        assertEquals(2,progress(g,emptyList(),start,start.plusDays(6),start.plusDays(5)).total)
    }
    @Test fun weekendRecordsIncreaseWeeklyTargetRegardlessOfCategory() {
        val records=listOf(Entry("g",start.plusDays(5),true),Entry("other",start,true))
        assertEquals(50,rate(goal(2),records).percent)
    }
    @Test fun archivedAndPartWeeksKeepChosenTarget() {
        val g=goal(3).copy(start=start.plusDays(2),end=start.plusDays(3))
        val p=rate(g,listOf(Entry("g",start.plusDays(2),true)))
        assertEquals(3,p.total)
        assertEquals(33,p.percent)
    }
    @Test fun requestedExamplesUseFullWeekAndTruncatePercent() {
        val weekdays=(0L..3L).map {Entry("g",start.plusDays(it),true)}
        assertEquals(80,rate(goal(),weekdays).percent)
        assertEquals(50,rate(goal(schedule=GoalSchedule.WEEKEND),listOf(Entry("g",start.plusDays(5),true))).percent)
        val mixed=listOf(Entry("g",start,true),Entry("g",start.plusDays(5),true))
        GoalSchedule.entries.forEach {schedule ->
            assertEquals(66,rate(goal(3,schedule),mixed).percent)
            assertEquals(100,rate(goal(3,schedule),mixed+Entry("g",start.plusDays(6),true)).percent)
        }
    }
    @Test fun weekdayRateDoesNotReachFullAfterJustMonday() {
        assertEquals(20,progress(goal(),listOf(Entry("g",start,true)),start,start.plusDays(6),start).percent)
    }
    @Test fun weeklyTargetIgnoresDayOverrides() {
        val g=goal(3).copy(dateSchedules=mapOf(start to GoalSchedule.WEEKEND))
        assertTrue(g.scheduled(start))
        assertTrue(g.scheduled(start.plusDays(5)))
        assertEquals(66,rate(g,listOf(Entry("g",start,true),Entry("g",start.plusDays(5),true))).percent)
    }}
