package com.together.daily

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Nationwide Korean holidays, including substitutes, verified for 2026–2027.
 * https://astro.kasi.re.kr/kor/life/post/calendarData?search_year=2026
 * https://astro.kasi.re.kr/kor/life/post/calendarData?search_year=2027
 * Labour Day / Constitution Day additions: KASA 2027 calendar announcement.
 * Update the annual table when new holidays are announced.
 */
object KoreanHolidays {
    const val FIRST_SUPPORTED_YEAR = 2026
    const val LAST_SUPPORTED_YEAR = 2027
    private val fixed = setOf("01-01", "03-01", "05-05", "06-06", "08-15", "10-03", "10-09", "12-25")
    private val extra = mapOf(
        2026 to setOf("02-16", "02-17", "02-18", "03-02", "05-01", "05-24", "05-25", "06-03", "07-17", "08-17", "09-24", "09-25", "09-26", "10-05"),
        2027 to setOf("02-06", "02-07", "02-08", "02-09", "05-01", "05-03", "05-13", "07-17", "07-19", "08-16", "09-14", "09-15", "09-16", "10-04", "10-11", "12-27")
    )
    fun isHoliday(date: LocalDate): Boolean {
        val key = date.toString().substring(5)
        return key in fixed || key in extra[date.year].orEmpty() ||
            (date.year >= 2026 && key in setOf("05-01", "07-17"))
    }
    fun isRestDay(date: LocalDate) = date.dayOfWeek == DayOfWeek.SATURDAY ||
        date.dayOfWeek == DayOfWeek.SUNDAY || isHoliday(date)
}
