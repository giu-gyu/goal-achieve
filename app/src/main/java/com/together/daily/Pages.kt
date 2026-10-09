package com.together.daily

import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
internal fun WelcomeArt(title:String,subtitle:String) {
    Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        TogetherArt(Modifier.size(150.dp))
        Text(title,fontSize=26.sp,lineHeight=36.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,letterSpacing=(-.7).sp)
        Text(subtitle,color=Muted,fontSize=13.sp,lineHeight=21.sp,textAlign=TextAlign.Center)
    }
}

@Composable
internal fun LoginPage(vm:DailyViewModel) {
    var email by rememberSaveable {mutableStateOf("")}
    var password by rememberSaveable {mutableStateOf("")}
    var signup by rememberSaveable {mutableStateOf(false)}
    var visible by rememberSaveable {mutableStateOf(false)}
    WelcomeArt("각자의 작은 목표,\n우리의 좋은 하루","서로의 하루를 응원하는 가장 다정한 방법")
    SoftCard {
        Segments(listOf("로그인","회원가입"),if(signup)1 else 0){signup=it==1}
        FieldLabel("이메일")
        OutlinedTextField(email,{email=it},singleLine=true,placeholder={Text("name@example.com",color=Muted,fontSize=14.sp)},
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),shape=RoundedCornerShape(15.dp),modifier=Modifier.fillMaxWidth())
        FieldLabel("비밀번호")
        OutlinedTextField(password,{password=it},singleLine=true,placeholder={Text("6자 이상 입력해주세요",color=Muted,fontSize=13.sp)},
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),
            visualTransformation=if(visible)VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon={TextButton(onClick={visible=!visible}){Text(if(visible)"숨기기" else "보기",fontSize=11.sp,color=Muted)}},
            shape=RoundedCornerShape(15.dp),modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(2.dp))
        PrimaryAction(if(signup)"쏘규 Daily 챌린지 시작하기" else "다시 만나서 반가워요",enabled=!vm.busy&&email.isNotBlank()&&password.length>=6,onClick={vm.login(email,password,signup)})
        if(!signup) TextButton(onClick={vm.resetPassword(email)},enabled=!vm.busy&&email.isNotBlank(),modifier=Modifier.align(Alignment.CenterHorizontally)) {Text("비밀번호가 기억나지 않아요",fontSize=12.sp,color=Muted)}
    }
    Text("각자의 계정으로 로그인하고,\n초대 코드로 두 사람의 하루를 연결해요.",color=Muted,fontSize=12.sp,lineHeight=20.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())
}

@Composable
private fun FieldLabel(text:String) {Text(text,color=Ink,fontSize=12.sp,fontWeight=FontWeight.SemiBold)}

@Composable
internal fun ConnectPage(vm:DailyViewModel) {
    var name by rememberSaveable {mutableStateOf("")}
    var code by rememberSaveable {mutableStateOf("")}
    var joining by rememberSaveable {mutableStateOf(false)}
    WelcomeArt("둘이어서\n더 좋은 시작","같은 공간에서, 서로의 작은 성공을 만나세요.")
    SoftCard {
        Segments(listOf("새 공간 만들기","초대받았어요"),if(joining)1 else 0){joining=it==1}
        FieldLabel("짝꿍이 부를 나의 이름")
        OutlinedTextField(name,{if(it.length<=20)name=it},singleLine=true,placeholder={Text("이름 또는 애칭",fontSize=14.sp,color=Muted)},shape=RoundedCornerShape(15.dp),modifier=Modifier.fillMaxWidth())
        if(joining) {
            FieldLabel("받은 초대 코드")
            OutlinedTextField(code,{code=it.trim()},singleLine=true,placeholder={Text("짝꿍에게 받은 코드를 붙여넣으세요",fontSize=12.sp,color=Muted)},shape=RoundedCornerShape(15.dp),modifier=Modifier.fillMaxWidth())
        } else Text("공간을 만든 뒤 초대 코드를 보내면\n둘만의 기록이 시작돼요.",color=Muted,fontSize=13.sp,lineHeight=21.sp)
        PrimaryAction(if(joining)"짝꿍과 연결하기" else "새 커플 공간 만들기",
            enabled=!vm.busy&&name.isNotBlank()&&(!joining||code.isNotBlank()),symbol=Symbol.Heart,
            onClick={vm.connect(name,if(joining)code else null)})
    }
    TextButton(onClick={vm.logout()},enabled=!vm.busy,modifier=Modifier.fillMaxWidth()){Text("다른 계정으로 로그인",color=Muted,fontSize=12.sp)}
}

@Composable
internal fun HistoryPage(vm:DailyViewModel,today:LocalDate) {
    var weekOffset by rememberSaveable {mutableLongStateOf(0L)}
    val start=weekStart(today).plusWeeks(weekOffset)
    val end=start.plusDays(6)
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
    Text("오늘도 화이팅!!",fontSize=18.sp,fontWeight=FontWeight.Bold)
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick={weekOffset--},modifier=Modifier.size(40.dp)) {Mark(Symbol.Back,description="전주")}
        Text("${start.monthValue}/${start.dayOfMonth} – ${end.monthValue}/${end.dayOfMonth} · ${start.year}",
            fontSize=13.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center,modifier=Modifier.weight(1f))
        IconButton(onClick={weekOffset++},modifier=Modifier.size(40.dp)) {Mark(Symbol.Next,description="다음주")}
        if(weekOffset!=0L) TextButton(onClick={weekOffset=0L},contentPadding=PaddingValues(horizontal=4.dp),modifier=Modifier.height(40.dp)) {Text("이번 주",fontSize=11.sp)}
    }
    val members=vm.names.entries.sortedBy {if(it.key==vm.uid)0 else 1}
    members.forEach {member ->
        WeeklyRecords(vm,member.key,"${member.value} · ${if(member.key==vm.uid) "나" else "짝꿍"}",start,end,today)
        vm.memos.filter {it.ownerId==member.key&&it.date>=start&&it.date<=end}.sortedBy {it.date}.forEach {memo ->
            Text("${memo.date.monthValue}/${memo.date.dayOfMonth} 메모",fontSize=11.sp,color=Muted)
            Text(memo.text,fontSize=12.sp,lineHeight=17.sp)
        }
    }
    if(members.size<2) Text("상대방이 연결되면 주간 기록이 여기에 표시됩니다.",color=Muted,fontSize=12.sp)
    }
}

@Composable
private fun WeeklyRecords(vm:DailyViewModel,ownerId:String,name:String,start:LocalDate,end:LocalDate,today:LocalDate) {
    val goals=vm.goals.filter {it.ownerId==ownerId&&it.start<=end&&(it.end==null||it.end>=start)}
    val records=vm.entries.associateBy {it.goalId to it.date}
    val days=(0L..6L).map {start.plusDays(it)}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(name,fontSize=14.sp,fontWeight=FontWeight.Bold)
        if(goals.isEmpty()) Text("이 주에 등록된 목표가 없습니다.",color=Muted,fontSize=12.sp)
        else Column(Modifier.fillMaxWidth().border(.5.dp,Muted.copy(alpha=.5f))) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(Lavender)) {
                Box(Modifier.weight(3f).fillMaxHeight().padding(4.dp),contentAlignment=Alignment.CenterStart) {Text("목표",fontSize=12.sp)}
                days.forEachIndexed {i,day ->
                    Column(Modifier.weight(1f).border(.5.dp,Muted.copy(alpha=.5f)).padding(vertical=3.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(listOf("월","화","수","목","금","토","일")[i],fontSize=11.sp)
                        Text(day.dayOfMonth.toString(),fontSize=10.sp,color=if(day==today)Coral else Muted)
                    }
                }
            }
            goals.forEach {goal ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Box(Modifier.weight(3f).fillMaxHeight().border(.5.dp,Muted.copy(alpha=.5f)).padding(4.dp),contentAlignment=Alignment.CenterStart) {Text(goal.title,fontSize=12.sp,lineHeight=15.sp)}
                    days.forEach {day ->
                        val done=records[goal.id to day]?.done
                        val applicable=goal.scheduled(day)&&day<=today
                        val label=if(!applicable||done==null) "" else if(done) "O" else "X"
                        Box(Modifier.weight(1f).fillMaxHeight().heightIn(min=28.dp).border(.5.dp,Muted.copy(alpha=.5f)).background(if(day==today)Peach.copy(alpha=.3f)else Color.White),contentAlignment=Alignment.Center) {
                            Text(label,fontSize=14.sp,fontWeight=FontWeight.Bold,color=if(done==true)Color(0xFF1565C0) else Color(0xFFC62828))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StatsPage(vm:DailyViewModel,today:LocalDate,onLogout:()->Unit) {
    var monthly by rememberSaveable {mutableStateOf(false)}
    var offset by rememberSaveable {mutableLongStateOf(0L)}
    val start=if(monthly)today.withDayOfMonth(1).plusMonths(offset) else weekStart(today).plusWeeks(offset)
    val end=if(monthly)YearMonth.from(start).atEndOfMonth() else start.plusDays(6)
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        ConnectionStatus(vm,onLogout)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("달성률",fontSize=14.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
            TextButton(onClick={monthly=false;offset=0},contentPadding=PaddingValues(horizontal=8.dp),modifier=Modifier.height(36.dp)) {Text("주간",fontSize=12.sp,color=if(!monthly)Coral else Muted)}
            TextButton(onClick={monthly=true;offset=0},contentPadding=PaddingValues(horizontal=8.dp),modifier=Modifier.height(36.dp)) {Text("월간",fontSize=12.sp,color=if(monthly)Coral else Muted)}
        }
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={offset--},modifier=Modifier.size(36.dp)) {Mark(Symbol.Back,description="이전 기간")}
            Text(if(monthly)"${start.year}년 ${start.monthValue}월" else "${start.monthValue}/${start.dayOfMonth} – ${end.monthValue}/${end.dayOfMonth} · ${start.year}",fontSize=12.sp,textAlign=TextAlign.Center,modifier=Modifier.weight(1f))
            IconButton(onClick={offset++},enabled=offset<0,modifier=Modifier.size(36.dp)) {Mark(Symbol.Next,color=if(offset<0)Ink else Line,description="다음 기간")}
        }
        vm.names.entries.sortedBy {if(it.key==vm.uid)0 else 1}.forEach {member ->
            val values=vm.goals.filter {it.ownerId==member.key}.map {progress(it,vm.entries,start,end,today)}
            val done=values.sumOf {it.done}
            val missed=values.sumOf {it.missed}
            val blank=values.sumOf {it.unrecorded}
            val total=done+missed+blank
            val percent=if(total==0)0 else (done*100.0/total).roundToInt()
            val emoji=when {total==0->"🌱";percent==100->"🏆";percent>=80->"😎";percent>=50->"😊";percent>0->"💪";else->"🌱"}
            Surface(color=Color.White,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text("${member.value} · ${if(member.key==vm.uid) "나" else "짝꿍"}",fontSize=13.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
                        Text(emoji,fontSize=23.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(if(total==0)"—" else "$percent%",fontSize=19.sp,fontWeight=FontWeight.Bold,color=Coral)
                    }
                    LinearProgressIndicator(progress={percent/100f},modifier=Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),color=Coral,trackColor=Peach,drawStopIndicator={})
                    Text(if(total==0)"이 기간에 기록할 목표가 없습니다." else "완료 $done · 미완료 $missed · 미기록 $blank",fontSize=11.sp,color=Muted)
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatus(vm:DailyViewModel,onLogout:()->Unit) {
    val clipboard=LocalClipboardManager.current
    var menu by remember {mutableStateOf(false)}
    var logout by remember {mutableStateOf(false)}
    val members=vm.names.entries.sortedBy {if(it.key==vm.uid)0 else 1}
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Text(if(members.size==2) "${members[0].value} ♥ ${members[1].value}" else "짝꿍을 기다리고 있어요",fontSize=13.sp,color=Coral,modifier=Modifier.weight(1f))
        Box {
            IconButton(onClick={menu=true},modifier=Modifier.size(36.dp)) {Mark(Symbol.More,color=Muted,description="계정 메뉴")}
            DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                if(members.size<2&&!vm.demo) DropdownMenuItem(text={Text("초대 코드 복사")},onClick={clipboard.setText(AnnotatedString(vm.pairId));menu=false})
                DropdownMenuItem(text={Text(if(vm.demo)"체험 마치기" else "로그아웃")},onClick={menu=false;logout=true},enabled=!vm.busy)
            }
        }
    }
    if(logout) AlertDialog(onDismissRequest={logout=false},title={Text("로그아웃할까요?")},
        confirmButton={TextButton(onClick={logout=false;onLogout()},enabled=!vm.busy){Text("로그아웃")}},
        dismissButton={TextButton(onClick={logout=false}){Text("취소")}})
}
