package com.together.daily

internal data class MemberEmojiStyle(val label:String,val male:String,val female:String)
internal val MemberEmojiStyles=listOf(
    MemberEmojiStyle("기쁨","😊","😊"),
    MemberEmojiStyle("슬픔","😢","😢"),
    MemberEmojiStyle("신남","🤩","🤩"),
    MemberEmojiStyle("서 있기","🧍‍♂️","🧍‍♀️"),
    MemberEmojiStyle("걷기","🚶‍♂️","🚶‍♀️"),
    MemberEmojiStyle("달리기","🏃‍♂️","🏃‍♀️"),
    MemberEmojiStyle("인사","🙋‍♂️","🙋‍♀️"),
    MemberEmojiStyle("놀람","😮","😮"),
    MemberEmojiStyle("행복","😄","😄"),
    MemberEmojiStyle("웃음","😆","😆"),
    MemberEmojiStyle("감동","🥹","🥹"),
    MemberEmojiStyle("사랑","🥰","🥰"),
    MemberEmojiStyle("반함","😍","😍"),
    MemberEmojiStyle("수줍음","☺️","☺️"),
    MemberEmojiStyle("장난","😜","😜"),
    MemberEmojiStyle("윙크","😉","😉"),
    MemberEmojiStyle("자신감","😎","😎"),
    MemberEmojiStyle("평온","😌","😌"),
    MemberEmojiStyle("생각","🤔","🤔"),
    MemberEmojiStyle("궁금","🧐","🧐"),
    MemberEmojiStyle("걱정","😟","😟"),
    MemberEmojiStyle("속상","😞","😞"),
    MemberEmojiStyle("화남","😠","😠"),
    MemberEmojiStyle("울음","😭","😭"),
    MemberEmojiStyle("졸림","😴","😴"),
    MemberEmojiStyle("피곤","😩","😩"),
    MemberEmojiStyle("축하","🥳","🥳"),
    MemberEmojiStyle("안도","😮‍💨","😮‍💨"),
    MemberEmojiStyle("당황","😳","😳"),
    MemberEmojiStyle("감사","🙏","🙏"),
    MemberEmojiStyle("결심","😤","😤"),
    MemberEmojiStyle("아픔","🤒","🤒")
)
internal fun memberEmoji(profile:MemberProfile):String {
    val style=MemberEmojiStyles.getOrElse(profile.emojiStyle) {MemberEmojiStyles.first()}
    return when(profile.gender) {
        ProfileGender.MALE->style.male
        ProfileGender.FEMALE->style.female
        null->if(style.male==style.female)style.male else "👤"
    }
}
