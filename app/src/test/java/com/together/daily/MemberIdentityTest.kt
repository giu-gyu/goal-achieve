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
    @Test fun legacyActionSelectionMigratesWithoutChangingDisplayedEmoji() {
        for(gender in listOf(ProfileGender.MALE,ProfileGender.FEMALE)) {
            for(index in 3..6) {
                val legacy=MemberProfile(gender=gender,emojiStyle=index)
                val migrated=MemberProfile(emojiStyle=selectedEmojiIndex(legacy))
                assertEquals(memberEmoji(legacy),memberEmoji(migrated))
                assertTrue(migrated.emojiStyle in SelectableMemberEmojiIndices)
            }
        }
    }
    @Test fun actionVariantsAreSelectableWithoutGender() {
        val expected=listOf("🧍‍♂️","🚶‍♂️","🏃‍♂️","🙋‍♂️","🧍‍♀️","🚶‍♀️","🏃‍♀️","🙋‍♀️")
        assertEquals(expected,(32..39).map {memberEmoji(MemberProfile(emojiStyle=it))})
    }
    @Test fun explicitThemeOverridesLegacyGenderAndInvalidThemeFallsBack() {
        assertEquals(20,ProfileThemes.size)
        assertEquals(20,ProfileThemes.map {it.rgb}.distinct().size)
        assertEquals(0,profileThemeIndex(MemberProfile(gender=ProfileGender.MALE)))
        assertEquals(1,profileThemeIndex(MemberProfile(gender=ProfileGender.FEMALE)))
        assertEquals(7,profileThemeIndex(MemberProfile(gender=ProfileGender.FEMALE,themeIndex=7)))
        assertEquals(18,profileThemeIndex(MemberProfile(themeIndex=999)))
    }
}
