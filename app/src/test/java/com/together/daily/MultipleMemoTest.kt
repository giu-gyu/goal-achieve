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
    @Test fun partnerBoardKeepsActualAuthorWhenEdited() {
        val memo=DailyMemo("me",date,"응원해",id="note",targetId="partner")
        val edited=memo.copy(text="오늘도 응원해")
        assertEquals("me",edited.ownerId)
        assertEquals("partner",edited.targetId)
        assertEquals("note",edited.id)
        assertEquals("partner",edited.reactedBy("partner","❤️").targetId)
    }
    @Test fun existingMemosDefaultToAuthorsBoard() {
        assertEquals("me",DailyMemo("me",date,"기존 메모").targetId)
    }
}
