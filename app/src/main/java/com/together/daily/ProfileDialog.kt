package com.together.daily

import androidx.compose.foundation.layout.*
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
    var resolve by rememberSaveable(vm.uid,profile.resolve) {mutableStateOf(profile.resolve)}
    AlertDialog(onDismissRequest=onDismiss,title={Text("내 프로필",fontSize=18.sp)},
        text={Column(Modifier.fillMaxWidth().heightIn(max=480.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name,{if(it.length<=20)name=it},label={Text("이름")},singleLine=true,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(bio,{if(it.length<=200)bio=it},label={Text("자기소개")},maxLines=3,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(resolve,{if(it.length<=200)resolve=it},label={Text("각오")},maxLines=3,modifier=Modifier.fillMaxWidth())
            TextButton(onClick={vm.saveProfile(name,bio,resolve)},enabled=!vm.busy&&name.isNotBlank()) {Text("프로필 저장")}
            Text("저장된 이름: ${vm.names[vm.uid].orEmpty()}",fontSize=11.sp,color=Muted)

        }},confirmButton={TextButton(onClick=onDismiss){Text("닫기")}})
}
