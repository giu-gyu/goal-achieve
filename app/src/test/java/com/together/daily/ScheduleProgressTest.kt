package com.together.daily
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class ScheduleProgressTest {
    private val start=LocalDate.parse("2026-11-02")
    private fun goal(target:Int=7)=Goal("g","me","걷기",start,weeklyTarget=target)
    private fun rate(g:Goal,records:List<Entry>,end:LocalDate=start.plusDays(6))=progress(g,records,start,end,end)
    @Test fun defaultIsDailyAndIncludesEveryDate() {
        assertEquals(7,goal().weeklyTarget)
        assertEquals("매일",goal().typeLabel)
        assertEquals(7,rate(goal(),emptyList()).total)
        assertEquals(85,rate(goal(),(0L..5L).map {Entry("g",start.plusDays(it),true)}).percent)
    }
    @Test fun everyTargetIsSupportedIncludingZero() {
        (0..7).forEach {assertEquals(it,rate(goal(it),emptyList()).total)}
        assertEquals(Progress(0,0,0),rate(goal(0),listOf(Entry("g",start,true))))
        assertEquals("주 0회",goal(0).typeLabel)
    }
    @Test fun targetCapsAndDeduplicatesCompletions() {
        val records=(0L..3L).map {Entry("g",start.plusDays(it),true)}
        assertEquals(33,rate(goal(3),listOf(records[0],records[0])).percent)
        assertEquals(100,rate(goal(3),records).percent)
    }
    @Test fun excessDoesNotCarryToNextWeek() {
        assertEquals(50,rate(goal(2),(0L..3L).map {Entry("g",start.plusDays(it),true)},start.plusDays(13)).percent)
    }
    @Test fun futureCompletionsAreExcluded() {
        assertEquals(0,progress(goal(2),listOf(Entry("g",start.plusDays(5),true)),start,start.plusDays(6),start).done)
    }
    @Test fun partialWeeksKeepTargetAndDatesRespectGoalLifetime() {
        val g=goal(3).copy(start=start.plusDays(2),end=start.plusDays(3))
        assertEquals(3,rate(g,emptyList()).total)
        assertFalse(g.scheduled(start))
        assertFalse(g.scheduled(start.plusDays(4)))
    }
}
