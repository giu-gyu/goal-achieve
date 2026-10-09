package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class RecordingStatusTest {
    private val day=LocalDate.of(2026,10,9)
    private val goals=listOf(Goal("a","me","A",day),Goal("b","me","B",day),Goal("c","partner","C",day))
    @Test fun incompleteResultCountsAsRecorded() {
        val status=recordingStatus(goals,listOf(Entry("a",day,true),Entry("b",day,false)),"me",day)
        assertTrue(status.allRecorded)
        assertEquals(0,status.missing)
    }
    @Test fun missingResultRequiresReminder() {
        val status=recordingStatus(goals,listOf(Entry("a",day,false),Entry("c",day,true)),"me",day)
        assertFalse(status.allRecorded)
        assertEquals(1,status.missing)
    }
    @Test fun noScheduledGoalsRequiresNoNotification() {
        val status=recordingStatus(goals,emptyList(),"me",day.minusDays(1))
        assertFalse(status.allRecorded)
        assertEquals(0,status.missing)
    }
    @Test fun duplicateAndOtherDatesDoNotInflateRecordedCount() {
        val status=recordingStatus(goals,listOf(Entry("a",day,true),Entry("a",day,false),Entry("b",day.minusDays(1),true)),"me",day)
        assertEquals(1,status.recorded)
        assertEquals(1,status.missing)
    }
}
