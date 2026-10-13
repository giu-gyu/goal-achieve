package com.together.daily

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileDialog(vm:DailyViewModel,onDismiss:()->Unit) {
    val profile=vm.profiles[vm.uid]?:MemberProfile()
    var name by rememberSaveable(vm.uid) {mutableStateOf(vm.names[vm.uid].orEmpty())}
    var bio by rememberSaveable(vm.uid,profile.bio) {mutableStateOf(profile.bio)}
    var gender by rememberSaveable(vm.uid,profile.gender) {mutableStateOf(profile.gender)}
    var emojiStyle by rememberSaveable(vm.uid,profile.emojiStyle) {mutableIntStateOf(profile.emojiStyle)}
    var resolve by rememberSaveable(vm.uid,profile.resolve) {mutableStateOf(profile.resolve)}
    AlertDialog(onDismissRequest={if(!vm.busy)onDismiss()},title={Text("내 프로필",fontSize=18.sp)},
        text={Column(Modifier.fillMaxWidth().heightIn(max=480.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name,{if(it.length<=20)name=it},label={Text("이름")},singleLine=true,modifier=Modifier.fillMaxWidth())
            Text("성별 · 남자 블루 / 여자 보라",fontSize=12.sp,color=Muted)
            Segments(listOf("남자","여자","미설정"),when(gender) {ProfileGender.MALE->0;ProfileGender.FEMALE->1;null->2},verticalPadding=8.dp) {
                gender=when(it) {0->ProfileGender.MALE;1->ProfileGender.FEMALE;else->null}
            }
            run {
                Text("감정 · 동작 이모티콘",fontSize=12.sp,color=Muted)
                MemberEmojiStyles.indices.toList().chunked(4).forEach {indices ->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                        indices.forEach {index ->
                            val style=MemberEmojiStyles[index]
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if(index==emojiStyle)Lavender else Color.White)
                                .selectable(selected=index==emojiStyle,enabled=!vm.busy,role=Role.RadioButton,onClick={emojiStyle=index})
                                .semantics {contentDescription=style.label}
                                .padding(vertical=7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)) {
                                Text(memberEmoji(MemberProfile(gender=gender,emojiStyle=index)),fontSize=24.sp)
                                Text(style.label,fontSize=10.sp,color=Muted)
                            }
                        }
                        repeat(4-indices.size) {Spacer(Modifier.weight(1f))}
                    }
                }
            }
            OutlinedTextField(bio,{if(it.length<=200)bio=it},label={Text("자기소개")},maxLines=3,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(resolve,{if(it.length<=200)resolve=it},label={Text("각오")},maxLines=3,modifier=Modifier.fillMaxWidth())

        }},confirmButton={Button(onClick={vm.saveProfile(name,bio,resolve,gender,emojiStyle,onSaved=onDismiss)},enabled=!vm.busy&&name.isNotBlank()) {Text(if(vm.busy)"저장 중…" else "저장")}},
        dismissButton={TextButton(onClick=onDismiss,enabled=!vm.busy){Text("취소")}})
}
