package com.together.daily

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileDialog(vm:DailyViewModel,onDismiss:()->Unit) {
    val context=LocalContext.current
    val profile=vm.profiles[vm.uid]?:MemberProfile()
    var name by rememberSaveable(vm.uid) {mutableStateOf(vm.names[vm.uid].orEmpty())}
    var bio by rememberSaveable(vm.uid,profile.bio) {mutableStateOf(profile.bio)}
    var resolve by rememberSaveable(vm.uid,profile.resolve) {mutableStateOf(profile.resolve)}
    val prefs=remember(vm.uid){AppAlerts.prefs(context,vm.uid)}
    var partner by rememberSaveable(vm.uid){mutableStateOf(prefs.getBoolean("partnerRecorded",true))}
    var goalChanges by rememberSaveable(vm.uid){mutableStateOf(prefs.getBoolean("goalChanges",false))}
    var reminder by rememberSaveable(vm.uid){mutableStateOf(prefs.getBoolean("reminder",false))}
    var hour by rememberSaveable(vm.uid){mutableIntStateOf(prefs.getInt("hour",21))}
    var minute by rememberSaveable(vm.uid){mutableIntStateOf(prefs.getInt("minute",0))}
    var status by remember {mutableStateOf("")}
    fun saveAlerts() {
        prefs.edit().putBoolean("partnerRecorded",partner).putBoolean("goalChanges",goalChanges).putBoolean("reminder",reminder).putInt("hour",hour).putInt("minute",minute).apply()
        AppAlerts.configure(context,vm.uid,vm.pairId)
        status="알림 설정을 저장했습니다."
    }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {granted ->
        if(granted)saveAlerts() else {status="알림 권한이 거부되었습니다. 폰 설정에서 앱 알림을 허용하세요."}
    }
    AlertDialog(onDismissRequest=onDismiss,title={Text("내 프로필 · 알림",fontSize=18.sp)},
        text={Column(Modifier.fillMaxWidth().heightIn(max=480.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name,{if(it.length<=20)name=it},label={Text("이름")},singleLine=true,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(bio,{if(it.length<=200)bio=it},label={Text("자기소개")},maxLines=3,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(resolve,{if(it.length<=200)resolve=it},label={Text("각오")},maxLines=3,modifier=Modifier.fillMaxWidth())
            TextButton(onClick={vm.saveProfile(name,bio,resolve)},enabled=!vm.busy&&name.isNotBlank()) {Text("프로필 저장")}
            Text("저장된 이름: ${vm.names[vm.uid].orEmpty()}",fontSize=11.sp,color=Muted)
            HorizontalDivider()
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("짝꿍이 오늘 목표를 모두 기록하면 알림",fontSize=12.sp,modifier=Modifier.weight(1f))
                Switch(partner,{partner=it})
            }
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("새 목표 추가나 목표 변경시 알람 받기",fontSize=12.sp,modifier=Modifier.weight(1f))
                Switch(goalChanges,{goalChanges=it})
            }
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("매일 기록 알림",fontSize=12.sp,modifier=Modifier.weight(1f))
                Switch(reminder,{reminder=it})
            }
            TextButton(onClick={TimePickerDialog(context,{_,h,m->hour=h;minute=m},hour,minute,true).show()},enabled=reminder) {Text("알림 시간 %02d:%02d (한국 시간)".format(hour,minute))}
            Text("짝꿍 알림은 앱을 열어 둔 동안만 받습니다. 백그라운드 서버 확인은 하지 않습니다. 시간 알림은 절전 상태에서 늦어질 수 있습니다.",fontSize=11.sp,color=Muted)
            TextButton(onClick={
                if((partner||goalChanges||reminder)&&Build.VERSION.SDK_INT>=33&&context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else saveAlerts()
            }) {Text("알림 설정 저장")}
            if(status.isNotEmpty())Text(status,fontSize=11.sp,color=Muted)
        }},confirmButton={TextButton(onClick=onDismiss){Text("닫기")}})
}
