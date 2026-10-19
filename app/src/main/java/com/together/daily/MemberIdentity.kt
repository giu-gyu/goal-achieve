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
    MemberEmojiStyle("아픔","🤒","🤒"),
    MemberEmojiStyle("남자 서있기","🧍‍♂️","🧍‍♂️"),
    MemberEmojiStyle("남자 걷기","🚶‍♂️","🚶‍♂️"),
    MemberEmojiStyle("남자 달리기","🏃‍♂️","🏃‍♂️"),
    MemberEmojiStyle("남자 인사","🙋‍♂️","🙋‍♂️"),
    MemberEmojiStyle("여자 서있기","🧍‍♀️","🧍‍♀️"),
    MemberEmojiStyle("여자 걷기","🚶‍♀️","🚶‍♀️"),
    MemberEmojiStyle("여자 달리기","🏃‍♀️","🏃‍♀️"),
    MemberEmojiStyle("여자 인사","🙋‍♀️","🙋‍♀️")
)
internal fun memberEmoji(profile:MemberProfile):String {
    val style=MemberEmojiStyles.getOrElse(profile.emojiStyle) {MemberEmojiStyles.first()}
    return when(profile.gender) {
        ProfileGender.MALE->style.male
        ProfileGender.FEMALE->style.female
        null->if(style.male==style.female)style.male else "👤"
    }
}

internal val SelectableMemberEmojiIndices=MemberEmojiStyles.indices.filter {it !in 3..6}
internal fun selectedEmojiIndex(profile:MemberProfile)=if(profile.emojiStyle in 3..6)
    32+profile.emojiStyle-3+(if(profile.gender==ProfileGender.FEMALE)4 else 0) else profile.emojiStyle
internal data class ProfileTheme(val label:String,val rgb:Long)
internal val ProfileThemes=listOf(
    ProfileTheme("블루",0xFF315F85),ProfileTheme("보라",0xFF72528D),
    ProfileTheme("코랄",0xFFAC4D43),ProfileTheme("로즈",0xFFAA4165),
    ProfileTheme("핑크",0xFFAD397E),ProfileTheme("라벤더",0xFF7462AC),
    ProfileTheme("인디고",0xFF4A50A0),ProfileTheme("네이비",0xFF304568),
    ProfileTheme("스카이",0xFF28759B),ProfileTheme("시안",0xFF137F8C),
    ProfileTheme("틸",0xFF217E78),ProfileTheme("민트",0xFF398169),
    ProfileTheme("그린",0xFF46743A),ProfileTheme("올리브",0xFF73722B),
    ProfileTheme("머스터드",0xFF916E19),ProfileTheme("오렌지",0xFFAC6024),
    ProfileTheme("레드",0xFFAB3E42),ProfileTheme("브라운",0xFF805C48),
    ProfileTheme("그레이",0xFF606B78),ProfileTheme("차콜",0xFF42464E)
)
internal fun profileThemeIndex(profile:MemberProfile?)=profile?.themeIndex?.takeIf {it in ProfileThemes.indices}
    ?:when(profile?.gender) {ProfileGender.MALE->0;ProfileGender.FEMALE->1;null->18}