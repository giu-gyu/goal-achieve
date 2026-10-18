package com.together.daily
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class MemoReactionTest {
    private val memo=DailyMemo("partner",LocalDate.of(2026,10,14),"힘내자")
    @Test fun multipleReactionsToggleIndependentlyForEachMember() {
        val reacted=memo.reactedBy("me","❤️").reactedBy("partner","👍").reactedBy("me","🔥")
        assertEquals(listOf("❤️","🔥"),reacted.reactions["me"])
        assertEquals(listOf("🔥"),reacted.reactedBy("me","❤️").reactions["me"])
        assertEquals(listOf("👍"),reacted.reactedBy("me","❤️").reactions["partner"])
        assertFalse(reacted.reactedBy("me","❤️").reactedBy("me","🔥").reactions.containsKey("me"))
    }
    @Test fun editingTextPreservesBothMembersReactions() {
        val reacted=memo.reactedBy("me","❤️").reactedBy("partner","👍")
        assertEquals(reacted.reactions,reacted.copy(text="수정한 메모").reactions)
    }
    @Test fun legacyAndNewReactionsLoadTogether() {
        assertEquals(mapOf("me" to listOf("❤️"),"partner" to listOf("👍","🔥")),
            memoReactions(mapOf("me" to "❤️","partner" to listOf("👍","🔥","👍","unknown"))))
    }
}
