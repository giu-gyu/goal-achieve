package com.together.daily

import org.junit.Assert.*
import org.junit.Test

class MemberIdentityTest {
    @Test fun existingSavedActionIndicesKeepTheirMeaningForBothPartners() {
        assertEquals("🧍‍♂️",memberEmoji(MemberProfile(gender=ProfileGender.MALE,emojiStyle=3)))
        assertEquals("🚶‍♀️",memberEmoji(MemberProfile(gender=ProfileGender.FEMALE,emojiStyle=4)))
        assertEquals("🏃‍♂️",memberEmoji(MemberProfile(gender=ProfileGender.MALE,emojiStyle=5)))
        assertEquals("🙋‍♀️",memberEmoji(MemberProfile(gender=ProfileGender.FEMALE,emojiStyle=6)))
    }
    @Test fun emotionSelectionWorksWithoutGenderAndInvalidStoredIndexHasFallback() {
        assertEquals("😢",memberEmoji(MemberProfile(emojiStyle=1)))
        assertEquals("😊",memberEmoji(MemberProfile(emojiStyle=999)))
    }
}
