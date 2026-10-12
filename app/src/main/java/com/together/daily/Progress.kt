package com.together.daily

import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

enum class GoalSchedule(val label: String) {
    WEEKDAY("평일"), WEEKEND("주말·공휴일");
    fun includes(date: LocalDate) = (this == WEEKEND) == KoreanHolidays.isRestDay(date)
}

data class Goal(val id: String, val ownerId: String, val title: String, val start: LocalDate, val end: LocalDate? = null, val order:Int=Int.MAX_VALUE, val schedule:GoalSchedule=GoalSchedule.WEEKDAY, val weeklyTarget:Int?=null, val dateSchedules:Map<LocalDate,GoalSchedule> = emptyMap(), val colorIndex:Int?=null) {
    val frequencyLabel get() = weeklyTarget?.let { "주 ${it}회" } ?: "해당 요일마다"
    fun scheduled(date: LocalDate) = date >= start && (end == null || date <= end) && schedule == scheduleForDate(date,dateSchedules)
}
fun scheduleForDate(date:LocalDate,overrides:Map<LocalDate,GoalSchedule> = emptyMap()):GoalSchedule =
    overrides[date] ?: if(KoreanHolidays.isRestDay(date)) GoalSchedule.WEEKEND else GoalSchedule.WEEKDAY

data class Entry(val goalId: String, val date: LocalDate, val done: Boolean)
data class Progress(val done: Int, val missed: Int, val unrecorded: Int) {
    val total get() = done + missed + unrecorded
    val percent get() = if (total == 0) 0 else (done * 100.0 / total).roundToInt()
}
fun progress(goal: Goal, entries: List<Entry>, start: LocalDate, end: LocalDate, today: LocalDate): Progress {
    val from = maxOf(start, goal.start)
    val until = minOf(end, today, goal.end ?: end)
    if (from > until) return Progress(0, 0, 0)
    val records = entries.filter { it.goalId == goal.id }.associateBy { it.date }
    var done = 0; var missed = 0; var blank = 0
    var date = from
    while (date <= until) {
        if (goal.scheduled(date)) when (records[date]?.done) { true -> done++; false -> missed++; null -> blank++ }
        date = date.plusDays(1)
    }
    goal.weeklyTarget?.let { target ->
        var week = weekStart(from)
        var completed = 0; var required = 0; var failed = 0
        while (week <= until) {
            val weekEnd = minOf(week.plusDays(6), until)
            val weekFrom = maxOf(week, from)
            val days = generateSequence(weekFrom) { it.plusDays(1) }.takeWhile { it <= weekEnd }
                .filter { goal.scheduled(it) }.toList()
            if (days.isNotEmpty()) {
                val successes = days.count { records[it]?.done == true }.coerceAtMost(target)
                val misses = days.count { records[it]?.done == false }.coerceAtMost(target - successes)
                required += target; completed += successes; failed += misses
            }
            week = week.plusWeeks(1)
        }
        return Progress(completed, failed, required - completed - failed)
    }
    return Progress(done, missed, blank)
}
fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

data class DailyMemo(val ownerId:String,val date:LocalDate,val text:String)
enum class ProfileGender { MALE, FEMALE }
data class MemberProfile(val bio:String="",val resolve:String="",val gender:ProfileGender?=null,val emojiStyle:Int=0)

fun successCount(goal:Goal,entries:List<Entry>,start:LocalDate,end:LocalDate,today:LocalDate):Int =
    entries.filter {it.goalId==goal.id&&it.done&&it.date>=start&&it.date<=end&&it.date<=today&&goal.scheduled(it.date)}.map {it.date}.distinct().size

fun daySuccessPercent(goals:List<Goal>,entries:List<Entry>,date:LocalDate,today:LocalDate):Int? {
    if(date>today)return null
    val scheduled=goals.filter {it.scheduled(date)}
    if(scheduled.isEmpty())return null
    val done=scheduled.count {g->entries.any {it.goalId==g.id&&it.date==date&&it.done}}
    return done*100/scheduled.size
}

fun allGoalsRecorded(goals:List<Goal>,entries:List<Entry>,date:LocalDate):Boolean =
    goals.isNotEmpty()&&goals.all {goal->entries.any {it.goalId==goal.id&&it.date==date}}