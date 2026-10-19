package com.together.daily

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth


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
    var monthly by rememberSaveable {mutableStateOf(false)}
    var weekOffset by rememberSaveable {mutableLongStateOf(0L)}
    val start=if(monthly)today.withDayOfMonth(1).plusMonths(weekOffset) else weekStart(today).plusWeeks(weekOffset)
    val end=if(monthly)YearMonth.from(start).atEndOfMonth() else start.plusDays(6)
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
        TextButton(onClick={monthly=false;weekOffset=0},modifier=Modifier.height(32.dp)) {Text("주간",fontSize=12.sp,color=if(!monthly)Coral else Muted)}
        TextButton(onClick={monthly=true;weekOffset=0},modifier=Modifier.height(32.dp)) {Text("월간",fontSize=12.sp,color=if(monthly)Coral else Muted)}
    }
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick={weekOffset--},modifier=Modifier.size(40.dp)) {Mark(Symbol.Back,description="이전 기간")}
        Text(if(monthly)"${start.year}년 ${start.monthValue}월" else "${start.monthValue}/${start.dayOfMonth} – ${end.monthValue}/${end.dayOfMonth} · ${start.year}",
            fontSize=13.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center,modifier=Modifier.weight(1f))
        IconButton(onClick={weekOffset++},modifier=Modifier.size(40.dp)) {Mark(Symbol.Next,description="다음 기간")}

    }
    if(weekOffset!=0L) TextButton(onClick={weekOffset=0L},contentPadding=PaddingValues(4.dp),modifier=Modifier.align(Alignment.CenterHorizontally).height(30.dp)) {Text(if(monthly)"이번 달" else "이번 주",fontSize=11.sp)}
    val members=vm.names.entries.sortedBy {if(it.key==vm.uid)0 else 1}
    members.forEach {member ->
        val palette=recordPalette(vm.profiles[member.key])
        Surface(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),color=palette.background,
            border=androidx.compose.foundation.BorderStroke(1.dp,palette.border)) {
            Column(Modifier.padding(horizontal=12.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                WeeklyRecords(vm,member.key,vm.memberLabel(member.key),start,end,today)
                val memos=vm.memos.filter {
                    it.targetId==member.key&&it.date>=start&&it.date<=end&&it.text.isNotBlank()
                }.sortedWith(compareBy<DailyMemo> {it.date}.thenBy {it.createdAt}.thenBy {it.id})
                if(memos.isNotEmpty()) {
                    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        memos.forEach {memo->key(memo.id) {MemoCard(vm,memo)}}
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
    if(members.size<2) Text("상대방이 연결되면 주간 기록이 여기에 표시됩니다.",color=Muted,fontSize=12.sp)
    }
}

private data class RecordPalette(val accent:Color,val background:Color,val header:Color,val border:Color,val cell:Color)

private fun recordPalette(profile:MemberProfile?):RecordPalette {
    val accent=Color(ProfileThemes[profileThemeIndex(profile)].rgb)
    fun tint(amount:Float)=androidx.compose.ui.graphics.lerp(Color.White,accent,amount)
    return RecordPalette(accent,tint(.06f),tint(.14f),tint(.28f),tint(.025f))
}
@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
internal fun MemoCard(vm:DailyViewModel,memo:DailyMemo) {
    val palette=recordPalette(vm.profiles[memo.ownerId])
    var menu by remember {mutableStateOf(false)}
    var editing by remember {mutableStateOf(false)}
    var draft by rememberSaveable(memo.id) {mutableStateOf(memo.text)}
    var copied by remember {mutableStateOf(false)}
    val clipboard=LocalClipboardManager.current
    val haptics=androidx.compose.ui.platform.LocalHapticFeedback.current
    val editable=memo.ownerId==vm.uid
    LaunchedEffect(copied) {if(copied) {kotlinx.coroutines.delay(1000);copied=false}}
    fun copyMemo() {
        clipboard.setText(AnnotatedString(memo.text))
        copied=true
    }
    Surface(modifier=Modifier.fillMaxWidth().combinedClickable(onClick={menu=true},
        onLongClickLabel="메모 메뉴",onLongClick={
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            menu=true
        }),shape=RoundedCornerShape(14.dp),color=if(copied)Peach else Color.White,
        border=androidx.compose.foundation.BorderStroke(1.dp,palette.border)) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Surface(color=palette.header,shape=RoundedCornerShape(7.dp)) {
                Text(memo.date.format(java.time.format.DateTimeFormatter.ofPattern("M월 d일 · E요일",java.util.Locale.KOREAN)),
                    fontSize=10.sp,fontWeight=FontWeight.SemiBold,color=palette.accent,
                    modifier=Modifier.padding(horizontal=8.dp,vertical=4.dp))
            }
            if(memo.ownerId!=memo.targetId) Text("작성자 · "+vm.names[memo.ownerId].orEmpty().ifBlank {"알 수 없음"},fontSize=11.sp,color=Muted,fontWeight=FontWeight.Medium)
            Text(memo.text,fontSize=14.sp,lineHeight=22.sp,color=Ink)
            if(memo.reactions.isNotEmpty()) Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                memo.reactions.values.flatten().groupingBy {it}.eachCount().forEach {(emoji,count) ->
                    Text(emoji+" "+count,fontSize=11.sp,modifier=Modifier.background(Cream,RoundedCornerShape(6.dp)).padding(horizontal=5.dp,vertical=2.dp))
                }
            }
        }
    }
    if(menu) AlertDialog(onDismissRequest={menu=false},containerColor=Cream,title={Text("메모 메뉴")},
        text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(memo.text,fontSize=14.sp,maxLines=6,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            if(editable) TextButton(onClick={draft=memo.text;menu=false;editing=true},enabled=!vm.busy) {Text("메모 수정")}
            TextButton(onClick={copyMemo()}) {Text(if(copied)"복사했어요" else "복사")}
            Text("이모티콘 선택 · 여러 개 선택할 수 있어요",fontSize=12.sp,color=Muted)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                MemoReactionEmojis.forEach {emoji ->
                    val selected=emoji in memo.reactions[vm.uid].orEmpty()
                    TextButton(onClick={vm.reactToMemo(memo,emoji)},enabled=!vm.busy,
                        colors=ButtonDefaults.textButtonColors(containerColor=if(selected)Peach else Color.Transparent),
                        modifier=Modifier.size(40.dp),contentPadding=PaddingValues(0.dp)) {Text(emoji,fontSize=22.sp)}
                }
            }
        }},confirmButton={TextButton(onClick={menu=false}) {Text("닫기")}})
    if(editing) AlertDialog(onDismissRequest={if(!vm.busy)editing=false},containerColor=Cream,
        title={Text("메모 수정")},
        text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(draft,{if(it.length<=2000)draft=it},enabled=!vm.busy,minLines=2,maxLines=6,modifier=Modifier.fillMaxWidth())
            if(draft.isBlank())Text("빈 내용으로 저장하면 이 메모만 삭제돼요.",fontSize=12.sp,color=Muted)
            vm.memoStatus?.takeUnless {it=="메모를 저장했어요."||it=="메모를 삭제했어요."}?.let {Text(it,color=Muted,fontSize=12.sp)}
        }},
        confirmButton={TextButton(onClick={vm.updateMemo(memo,draft,onSaved={editing=false})},enabled=!vm.busy&&draft.trim()!=memo.text) {Text("저장")}},
        dismissButton={TextButton(onClick={editing=false},enabled=!vm.busy) {Text("취소")}})
}
@Composable
private fun WeeklyRecords(vm:DailyViewModel,ownerId:String,name:String,start:LocalDate,end:LocalDate,today:LocalDate) {
    val palette=recordPalette(vm.profiles[ownerId])
    val goals=vm.goals.filter {it.ownerId==ownerId&&it.start<=end&&(it.end==null||it.end>=start)}
    val records=vm.entries.associateBy {it.goalId to it.date}
    val days=generateSequence(start){it.plusDays(1)}.takeWhile {it<=end}.toList()
    val weekBoundaries=days.zipWithNext().filter {(previous,next)->weekStart(previous)!=weekStart(next)}.map {it.second}.toSet()
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text(name,fontSize=13.sp,fontWeight=FontWeight.Bold,color=palette.accent,modifier=Modifier.weight(1f),maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        if(goals.any {it.start>start}) Text("회색 — : 목표 생성 전",fontSize=10.sp,color=Muted)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val tableWidth=if(days.size>7)maxOf(maxWidth,(124+days.size*28).dp)else maxWidth
            Column(if(days.size>7)Modifier.horizontalScroll(rememberScrollState()) else Modifier) {
            Column(Modifier.width(tableWidth).clip(RoundedCornerShape(12.dp)).background(palette.cell).border(.5.dp,palette.border,RoundedCornerShape(12.dp))) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(palette.header)) {
                Box(Modifier.weight(3f).fillMaxHeight().padding(4.dp),contentAlignment=Alignment.CenterStart) {Text("목표",fontSize=12.sp)}
                days.forEachIndexed {i,day ->
                    Column(Modifier.weight(1f).border(.5.dp,palette.border).weekBoundary(day in weekBoundaries,palette.accent).padding(vertical=2.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(androidx.compose.ui.text.buildAnnotatedString {
                            pushStyle(androidx.compose.ui.text.SpanStyle(fontSize=12.sp,fontWeight=FontWeight.Bold))
                            append(day.dayOfMonth.toString());pop();append("\n")
                            pushStyle(androidx.compose.ui.text.SpanStyle(fontSize=9.sp))
                            append(listOf("월","화","수","목","금","토","일")[day.dayOfWeek.value-1]);pop()
                        },lineHeight=12.sp,textAlign=TextAlign.Center,color=if(day==today)palette.accent else Muted,
                            style=androidx.compose.ui.text.TextStyle(platformStyle=androidx.compose.ui.text.PlatformTextStyle(includeFontPadding=false)))
                    }
                }
                Box(Modifier.weight(1.25f).fillMaxHeight().border(.5.dp,palette.border),contentAlignment=Alignment.Center) {Text("달성률",fontSize=10.sp)}
            }
            goals.forEach {goal ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Row(Modifier.weight(3f).fillMaxHeight().background(goal.typeBackground).border(.5.dp,palette.border).padding(4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        Text(goal.title,fontSize=12.sp,lineHeight=15.sp,color=Ink,modifier=Modifier.weight(1f))
                        if(goal.weeklyTarget!=7) {
                            Surface(color=Color.White.copy(alpha=.8f),shape=RoundedCornerShape(5.dp),border=androidx.compose.foundation.BorderStroke(.5.dp,goal.typeAccent.copy(alpha=.3f))) {
                                Text(goal.frequencyLabel,fontSize=8.sp,color=goal.typeAccent,modifier=Modifier.padding(horizontal=3.dp,vertical=2.dp),maxLines=1)
                            }
                        }
                    }
                    days.forEach {day ->
                        val done=records[goal.id to day]?.done
                        val beforeCreation=day<goal.start
                        val applicable=goal.scheduled(day)&&day<=today
                        val label=if(beforeCreation) "—" else if(!applicable||done==null) "" else if(done) "O" else "X"
                        Box(Modifier.weight(1f).fillMaxHeight().heightIn(min=24.dp).border(.5.dp,palette.border).background(if(beforeCreation)Color(0xFFE7E9EC) else if(day==today)palette.header else palette.cell).weekBoundary(day in weekBoundaries,palette.accent)
                            .semantics {if(beforeCreation)contentDescription=day.toString()+", 목표 생성 전"},contentAlignment=Alignment.Center) {
                            Text(label,fontSize=14.sp,fontWeight=FontWeight.Bold,color=if(beforeCreation)Color(0xFF858B94) else if(done==true)Color(0xFF1565C0) else Color(0xFFC62828))
                        }
                    }
                    Box(Modifier.weight(1.25f).fillMaxHeight().border(.5.dp,palette.border),contentAlignment=Alignment.Center) {Text(progress(goal,vm.entries,start,end,today).let {if(it.total==0)"—" else "${it.percent}%"},fontSize=11.sp,fontWeight=FontWeight.Bold)}
                }
            }
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(palette.header)) {
                Box(Modifier.weight(3f).fillMaxHeight().border(.5.dp,palette.border).padding(4.dp)) {Text("당일 완료율",fontSize=11.sp)}
                days.forEach {day ->
                    val value=daySuccessPercent(goals,vm.entries,day,today)
                    Box(Modifier.weight(1f).fillMaxHeight().heightIn(min=24.dp).border(.5.dp,palette.border).weekBoundary(day in weekBoundaries,palette.accent),contentAlignment=Alignment.Center) {Text(value?.let {"$it%"}?:"",fontSize=9.sp)}
                }
                Box(Modifier.weight(1.25f).fillMaxHeight().border(.5.dp,palette.border),contentAlignment=Alignment.Center) {Text("기간",fontSize=10.sp)}
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
            val ownGoals=vm.goals.filter {it.ownerId==member.key}
            val through=achievementCutoff(ownGoals,vm.entries,today)
            val ranked=ownGoals.filter {it.start<=minOf(end,through)&&(it.end==null||it.end>=start)}
                .map {it to progress(it,vm.entries,start,minOf(end,through),through)}
                .sortedWith(compareByDescending<Pair<Goal,Progress>> {(_,p)->if(p.total==0)-1.0 else p.done.toDouble()/p.total}.thenByDescending {it.second.done}.thenBy {it.first.title})
            val values=ranked.map {it.second}
            val done=values.sumOf {it.done}
            val missed=values.sumOf {it.missed}
            val blank=values.sumOf {it.unrecorded}
            val total=done+missed+blank
            val percent=if(total==0)0 else (done*100.0/total).toInt()
            val emoji=when {total==0->"🌱";percent==100->"🏆";percent>=80->"😎";percent>=50->"😊";percent>0->"💪";else->"🌱"}
            Surface(color=Color.White,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(vm.memberLabel(member.key),fontSize=13.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
                        Text(emoji,fontSize=23.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(if(total==0)"—" else "$percent%",fontSize=19.sp,fontWeight=FontWeight.Bold,color=Coral)
                    }
                    LinearProgressIndicator(progress={percent/100f},modifier=Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),color=Coral,trackColor=Peach,drawStopIndicator={})
                    Text(if(total==0)"이 기간에 기록할 목표가 없습니다." else "완료 $done · 미완료 $missed · 미기록 $blank",fontSize=11.sp,color=Muted)
                    ranked.forEachIndexed {index,(goal,p) ->
                        HorizontalDivider(color=Line)
                        Row(Modifier.fillMaxWidth().background(goal.typeBackground,RoundedCornerShape(10.dp)).padding(8.dp),verticalAlignment=Alignment.CenterVertically) {
                            Text(listOf("${index+1}. ${goal.title}",goal.typeLabel).filter {it.isNotBlank()}.joinToString(" · "),color=goal.typeAccent,fontSize=12.sp,lineHeight=16.sp,modifier=Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            Text(if(p.total==0)"—" else "${p.percent}%",fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=goal.typeAccent)
                        }
                    }
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

private fun Modifier.weekBoundary(visible:Boolean,color:Color):Modifier = if(!visible)this else drawWithContent {
    drawContent()
    drawLine(color=color,start=androidx.compose.ui.geometry.Offset(1.25.dp.toPx(),0f),
        end=androidx.compose.ui.geometry.Offset(1.25.dp.toPx(),size.height),strokeWidth=2.5.dp.toPx())
}