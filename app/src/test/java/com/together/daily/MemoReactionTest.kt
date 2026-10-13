package com.together.daily

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MemoReactionTest {
    private val memo=DailyMemo("partner",LocalDate.of(2026,10,14),"힘내자")
    @Test fun eachMemberReactionIsIndependentAndCanBeRemoved() {
        val reacted=memo.reactedBy("me","❤️").reactedBy("partner","👍")
        assertEquals(mapOf("me" to "❤️","partner" to "👍"),reacted.reactions)
        assertEquals(mapOf("partner" to "👍"),reacted.reactedBy("me","❤️").reactions)
        assertEquals("🔥",reacted.reactedBy("me","🔥").reactions["me"])
    }
    @Test fun editingTextPreservesBothMembersReactions() {
        val reacted=memo.reactedBy("me","❤️").reactedBy("partner","👍")
        assertEquals(reacted.reactions,reacted.copy(text="수정한 메모").reactions)
    }
}