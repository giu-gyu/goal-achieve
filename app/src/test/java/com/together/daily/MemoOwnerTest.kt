package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MemoOwnerTest {
    private val date=LocalDate.parse("2026-10-12")
    @Test fun ownerSwitchReadsOnlySelectedOwnersMemo() {
        val memos=listOf(DailyMemo("me",date,"이메모가 보이니"),DailyMemo("partner",date,"짝꿍 메모"))
        assertEquals("이메모가 보이니",memos.memoFor("me",date))
        assertEquals("짝꿍 메모",memos.memoFor("partner",date))
    }
    @Test fun missingPartnerMemoNeverFallsBackToMineOrAnotherDate() {
        val memos=listOf(DailyMemo("me",date,"이메모가 보이니"),DailyMemo("partner",date.minusDays(1),"어제 메모"))
        assertEquals("",memos.memoFor("partner",date))
        assertEquals("",memos.memoFor("unknown",date))
    }
}
