package com.together.daily

import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters


data class Goal(val id:String,val ownerId:String,val title:String,val start:LocalDate,val end:LocalDate?=null,val order:Int=Int.MAX_VALUE,val weeklyTarget:Int=7) {
    init { require(weeklyTarget in 0..7) }
    val frequencyLabel get() = if(weeklyTarget==7) "매일" else "주 ${weeklyTarget}회"
    val typeLabel get() = frequencyLabel
    fun scheduled(date:LocalDate) = date>=start && (end==null || date<=end)
}

data class Entry(val goalId: String, val date: LocalDate, val done: Boolean)
data class Progress(val done: Int, val missed: Int, val unrecorded: Int) {
    val total get() = done + missed + unrecorded
    val percent get() = if (total == 0) 0 else (done * 100.0 / total).toInt()
}
fun progress(goal: Goal, entries: List<Entry>, start: LocalDate, end: LocalDate, today: LocalDate): Progress {
    val from = maxOf(start, goal.start)
    val until = minOf(end, goal.end ?: end)
    if (from > until || from > today) return Progress(0, 0, 0)
    val records = entries.filter { it.goalId == goal.id && it.date <= today }.associateBy { it.date }
    var done = 0; var missed = 0; var blank = 0
    var date = from
    while (date <= until) {
        if (goal.scheduled(date)) when (records[date]?.done) { true -> done++; false -> missed++; null -> blank++ }
        date = date.plusDays(1)
    }
    goal.weeklyTarget.takeIf {it!=7}?.let { target ->
        var week = weekStart(from)
        var completed = 0; var required = 0; var failed = 0
        while (week <= minOf(until,today)) {
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
fun achievementCutoff(goals:List<Goal>,entries:List<Entry>,today:LocalDate):LocalDate {
    val todaysGoals=goals.filter {it.scheduled(today)}.map {it.id}.toSet()
    return if(entries.any {it.date==today&&it.goalId in todaysGoals})today else today.minusDays(1)
}
fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

val MemoReactionEmojis=listOf("❤️","👍","👏","🥰","🔥","😂")
data class DailyMemo(val ownerId:String,val date:LocalDate,val text:String,val reactions:Map<String,String> = emptyMap()) {
    fun reactedBy(memberId:String,emoji:String):DailyMemo {
        require(memberId.isNotBlank()&&emoji in MemoReactionEmojis)
        return copy(reactions=if(reactions[memberId]==emoji)reactions-memberId else reactions+(memberId to emoji))
    }
}
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

fun List<DailyMemo>.memoFor(ownerId:String,date:LocalDate):String = firstOrNull {it.ownerId==ownerId&&it.date==date}?.text.orEmpty()
