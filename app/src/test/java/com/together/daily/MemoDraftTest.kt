package com.together.daily

import org.junit.Assert.*
import org.junit.Test

class MemoDraftTest {
    @Test fun initialServerLoadUpdatesUntouchedDraft() {
        assertEquals(MemoDraft("saved","saved"),MemoDraft().remote("saved"))
    }
    @Test fun delayedListenerAndRejectedWriteDoNotEraseTyping() {
        val draft=MemoDraft("new text","old")
        assertEquals(draft,draft.remote("old"))
        assertEquals(draft,draft.remote("other device edit"))
    }
    @Test fun confirmedSaveTrimsAndSetsBaseline() {
        assertEquals(MemoDraft("new","new"),MemoDraft(" new ","old").confirmed(" new "))
    }
    @Test fun typingWhileSavingSurvivesConfirmation() {
        assertEquals(MemoDraft("newer","new"),MemoDraft("newer","old").confirmed("new"))
    }
    @Test fun clearingMemoIsConfirmedAsEmpty() {
        assertEquals(MemoDraft(),MemoDraft("  ","saved").confirmed("  "))
    }
}
