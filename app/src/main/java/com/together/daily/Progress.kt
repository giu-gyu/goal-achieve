package com.together.daily

import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

data class Goal(val id: String, val ownerId: String, val title: String, val start: LocalDate, val end: LocalDate? = null, val order:Int=Int.MAX_VALUE) {
    fun scheduled(date: LocalDate) = date >= start && (end == null || date <= end)
}
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
        when (records[date]?.done) { true -> done++; false -> missed++; null -> blank++ }
        date = date.plusDays(1)
    }
    return Progress(done, missed, blank)
}
fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

data class DailyMemo(val ownerId:String,val date:LocalDate,val text:String)
data class MemberProfile(val bio:String="",val resolve:String="")

fun successCount(goal:Goal,entries:List<Entry>,start:LocalDate,end:LocalDate,today:LocalDate):Int =
    entries.filter {it.goalId==goal.id&&it.done&&it.date>=start&&it.date<=end&&it.date<=today&&goal.scheduled(it.date)}.map {it.date}.distinct().size

fun daySuccessPercent(goals:List<Goal>,entries:List<Entry>,date:LocalDate,today:LocalDate):Int? {
    if(date>today)return null
    val scheduled=goals.filter {it.scheduled(date)}
    if(scheduled.isEmpty())return null
    val done=scheduled.count {g->entries.any {it.goalId==g.id&&it.date==date&&it.done}}
    return done*100/scheduled.size
}

data class DailyRecordingStatus(val total:Int,val recorded:Int) {
    val allRecorded get()=total>0&&recorded==total
    val missing get()=total-recorded
}
fun recordingStatus(goals:List<Goal>,entries:List<Entry>,ownerId:String,date:LocalDate):DailyRecordingStatus {
    val scheduled=goals.filter {it.ownerId==ownerId&&it.scheduled(date)}
    return DailyRecordingStatus(scheduled.size,scheduled.count {goal->entries.any {it.goalId==goal.id&&it.date==date}})
}
