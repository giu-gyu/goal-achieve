package com.together.daily

internal data class MemberEmojiStyle(val label:String,val male:String,val female:String)
internal val MemberEmojiStyles=listOf(
    MemberEmojiStyle("기본 얼굴","👨","👩"),
    MemberEmojiStyle("곱슬머리","👨‍🦱","👩‍🦱"),
    MemberEmojiStyle("금발","👱‍♂️","👱‍♀️"),
    MemberEmojiStyle("서 있기","🧍‍♂️","🧍‍♀️"),
    MemberEmojiStyle("걷기","🚶‍♂️","🚶‍♀️"),
    MemberEmojiStyle("달리기","🏃‍♂️","🏃‍♀️"),
    MemberEmojiStyle("인사","🙋‍♂️","🙋‍♀️")
)
internal fun memberEmoji(profile:MemberProfile):String {
    val style=MemberEmojiStyles.getOrElse(profile.emojiStyle) {MemberEmojiStyles.first()}
    return when(profile.gender) {
        ProfileGender.MALE->style.male
        ProfileGender.FEMALE->style.female
        null->"👤"
    }
}
