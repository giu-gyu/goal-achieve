package com.together.daily
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class MultipleMemoTest {
    private val date=LocalDate.of(2026,10,18)
    @Test fun addingAnotherMemoOnSameDayPreservesLegacyMemo() {
        val first=DailyMemo("me",date,"첫 메모")
        val next=DailyMemo("me",date,"다음 메모",id="new",createdAt=1)
        val memos=listOf(first).withMemo(next)
        assertEquals(2,memos.size)
        assertEquals("첫 메모\n다음 메모",memos.memoFor("me",date))
    }
    @Test fun updatingOrRetryingOneIdDoesNotReplaceOtherMemos() {
        val first=DailyMemo("me",date,"첫 메모",id="first")
        val second=DailyMemo("me",date,"둘째 메모",id="second")
        val updated=listOf(first,second).withMemo(second.copy(text="수정"))
        assertEquals(listOf(first,second.copy(text="수정")),updated)
        assertEquals(updated,updated.withMemo(updated.last()))
    }
}
