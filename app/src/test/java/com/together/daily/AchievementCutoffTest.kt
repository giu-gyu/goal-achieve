package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AchievementCutoffTest {
    private val monday=LocalDate.of(2026,11,2)
    private val goal=Goal("g","me","운동",monday)
    private fun rate(goals:List<Goal>,entries:List<Entry>,today:LocalDate):Progress {
        val through=achievementCutoff(goals,entries,today)
        return progress(goals.first(),entries,monday,minOf(monday.plusDays(6),through),through)
    }
    @Test fun completedThroughTuesdayIsFullAndUntouchedWednesdayKeepsIt() {
        val entries=listOf(Entry("g",monday,true),Entry("g",monday.plusDays(1),true))
        assertEquals(100,rate(listOf(goal),entries,monday.plusDays(1)).percent)
        assertEquals(100,rate(listOf(goal),entries,monday.plusDays(2)).percent)
    }
    @Test fun startingAnyOwnGoalIncludesTodayForEveryOwnGoal() {
        val other=goal.copy(id="other")
        val entries=listOf(Entry("g",monday,true),Entry("g",monday.plusDays(1),true),Entry("other",monday.plusDays(2),false))
        assertEquals(monday.plusDays(2),achievementCutoff(listOf(goal,other),entries,monday.plusDays(2)))
        assertEquals(66,rate(listOf(goal,other),entries,monday.plusDays(2)).percent)
    }
    @Test fun partnerAndFutureRecordsDoNotStartTodaysOwnGoals() {
        val entries=listOf(Entry("g",monday,true),Entry("g",monday.plusDays(1),true),Entry("partner",monday.plusDays(2),true),Entry("g",monday.plusDays(3),true))
        assertEquals(100,rate(listOf(goal),entries,monday.plusDays(2)).percent)
    }
    @Test fun weeklyTargetKeepsItsChosenCount() {
        val g=goal.copy(weeklyTarget=3)
        val entries=listOf(Entry("g",monday,true),Entry("g",monday.plusDays(1),true))
        assertEquals(66,rate(listOf(g),entries,monday.plusDays(2)).percent)
    }
}