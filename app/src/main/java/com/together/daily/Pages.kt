package com.together.daily

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
    }
    if(members.size<2) Text("상대방이 연결되면 주간 기록이 여기에 표시됩니다.",color=Muted,fontSize=12.sp)
    }
}

@Composable
private fun WeeklyRecords(vm:DailyViewModel,ownerId:String,name:String,start:LocalDate,end:LocalDate,today:LocalDate) {
    val goals=vm.goals.filter {it.ownerId==ownerId&&it.start<=end&&(it.end==null||it.end>=start)}
    val records=vm.entries.associateBy {it.goalId to it.date}
    val days=(0L..6L).map {start.plusDays(it)}
    Surface(color=Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text(name,fontSize=15.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
            if(goals.isNotEmpty()) Text("좌우 스크롤 ↔",color=Muted,fontSize=10.sp)
        }
        if(goals.isEmpty()) Text("이 주에 등록된 목표가 없습니다.",color=Muted,fontSize=13.sp)
        else {
            Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Row(Modifier.height(IntrinsicSize.Min).background(Lavender.copy(alpha=.5f)),verticalAlignment=Alignment.CenterVertically) {
                    Text("목표",modifier=Modifier.width(140.dp).padding(horizontal=8.dp,vertical=4.dp),fontWeight=FontWeight.SemiBold,fontSize=13.sp)
                    days.forEachIndexed {index,day ->
                        Column(Modifier.width(56.dp).padding(vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(0.dp)) {
                            Text(listOf("월","화","수","목","금","토","일")[index],fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=if(day==today)Coral else Ink)
                            Text("${day.monthValue}/${day.dayOfMonth}",fontSize=10.sp,color=if(day==today)Coral else Muted)
                        }
                    }
                }
                goals.forEach {goal ->
                    HorizontalDivider(color=Line)
                    Row(Modifier.height(IntrinsicSize.Min),verticalAlignment=Alignment.CenterVertically) {
                        Text(goal.title,modifier=Modifier.width(140.dp).heightIn(min=32.dp).wrapContentHeight().padding(horizontal=8.dp,vertical=4.dp),fontSize=13.sp,lineHeight=17.sp,fontWeight=FontWeight.Medium)
                        days.forEach {day ->
                            val done=records[goal.id to day]?.done
                            val label=when {
                                !goal.scheduled(day)->"—"
                                day>today->"예정"
                                done==true->"완료"
                                done==false->"미완료"
                                else->"미기록"
                            }
                            val color=when(label) {"완료"->Sage;"미완료"->Coral;else->Muted}
                            Box(Modifier.width(56.dp).fillMaxHeight().background(if(day==today)Peach.copy(alpha=.25f) else Color.Transparent),contentAlignment=Alignment.Center) {
                                Text(label,color=color,fontSize=11.sp,fontWeight=if(done==true)FontWeight.Bold else FontWeight.Normal)
                            }
                        }
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
    var anchorText by rememberSaveable {mutableStateOf(today.toString())}
    var owner by rememberSaveable {mutableIntStateOf(0)}
    val anchor=LocalDate.parse(anchorText)
    val start=if(monthly)anchor.withDayOfMonth(1)else weekStart(anchor)
    val end=if(monthly)YearMonth.from(anchor).atEndOfMonth()else start.plusDays(6)
    ConnectionStatus(vm,onLogout)
    Segments(listOf("이번 주의 발걸음","한 달의 발걸음"),if(monthly)1 else 0){monthly=it==1}
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick={anchorText=(if(monthly)start.minusMonths(1)else start.minusWeeks(1)).toString()}) {Mark(Symbol.Back,description="이전 기간")}
        Text(if(monthly)"${start.year}년 ${start.monthValue}월" else "${start.monthValue}월 ${start.dayOfMonth}일 – ${end.monthValue}월 ${end.dayOfMonth}일",
            fontSize=14.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center,modifier=Modifier.weight(1f))
        val next=if(monthly)start.plusMonths(1)else start.plusWeeks(1)
        IconButton(onClick={anchorText=next.toString()},enabled=next<=today) {Mark(Symbol.Next,color=if(next<=today)Ink else Line,description="다음 기간")}
    }
    val id=OwnerSwitch(vm,owner){owner=it}
    val list=vm.goals.filter {it.ownerId==id&&it.start<=minOf(end,today)&&(it.end==null||it.end>=start)}
    val values=list.map {progress(it,vm.entries,start,end,today)}
    val done=values.sumOf {it.done}; val missed=values.sumOf {it.missed}; val blank=values.sumOf {it.unrecorded}
    val total=done+missed+blank
    val percent=if(total==0)0 else (done*100.0/total).roundToInt()
    SoftCard(color=Lavender.copy(alpha=.65f)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
            ProgressRing(percent)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("전체 목표 달성률",color=Lilac,fontSize=12.sp,fontWeight=FontWeight.Medium)
                Text(if(total==0)"시작하는 마음도\n충분히 멋져요" else if(percent>=80)"정말 잘하고 있어요\n이대로 함께 걸어요" else "한 걸음도 소중해요\n우리의 속도로 가요",
                    fontSize=18.sp,lineHeight=27.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.4).sp)
            }
        }
        HorizontalDivider(color=Lilac.copy(alpha=.12f))
        Row(Modifier.fillMaxWidth()) {
            Metric("완료",done,Sage,Modifier.weight(1f))
            Metric("미완료",missed,Lilac,Modifier.weight(1f))
            Metric("미기록",blank,Muted,Modifier.weight(1f))
        }
    }
    Text("목표별 발걸음",fontSize=18.sp,fontWeight=FontWeight.Bold)
    if(list.isEmpty()) EmptyGoals("기록은 이제부터 차곡차곡","이 기간에 진행한 목표가 없어요.")
    list.forEachIndexed {i,goal ->
        val p=values[i]
        SoftCard {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).background(Peach.copy(alpha=.7f),RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center) {Mark(goalSymbol(goal.title),color=Coral,modifier=Modifier.size(18.dp))}
                Text(goal.title,fontSize=14.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
                Text("${p.percent}%",fontSize=22.sp,color=Coral,fontWeight=FontWeight.Bold)
            }
            LinearProgressIndicator(progress={p.percent/100f},modifier=Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),color=Coral,trackColor=Peach)
            Text("완료 ${p.done}일  ·  미완료 ${p.missed}일  ·  미기록 ${p.unrecorded}일",fontSize=11.sp,color=Muted)
            if(goal.end!=null)Text("마무리한 약속",fontSize=10.sp,color=Lilac)
        }
    }
    Text("오늘까지의 기록을 집계해요. 미완료·미기록도 전체 날짜에 포함하며, 목표 시작 전과 종료 후는 제외해요.",color=Muted,fontSize=11.sp,lineHeight=18.sp)
}

@Composable
private fun ProgressRing(percent:Int) {
    val animated by animateFloatAsState(percent.toFloat(),label="progress")
    Box(Modifier.size(103.dp),contentAlignment=Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke=8.dp.toPx()
            val inset=stroke/2
            val diameter=size.minDimension-stroke
            drawArc(Color.White,-90f,360f,false,Offset(inset,inset),Size(diameter,diameter),style=Stroke(stroke,cap=StrokeCap.Round))
            if(animated>0)drawArc(Lilac,-90f,animated*3.6f,false,Offset(inset,inset),Size(diameter,diameter),style=Stroke(stroke,cap=StrokeCap.Round))
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Text("$percent%",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Ink)
            Text("차곡차곡",fontSize=10.sp,color=Lilac)
        }
    }
}
@Composable
private fun Metric(label:String,value:Int,color:Color,modifier:Modifier) {
    Column(modifier,horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Text(value.toString(),fontSize=21.sp,fontWeight=FontWeight.Bold,color=color)
        Text(label,fontSize=11.sp,color=Muted)
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
