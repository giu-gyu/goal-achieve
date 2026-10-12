package com.together.daily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LegacyNotificationCleanup.cancel(this)
        enableEdgeToEdge(
            statusBarStyle=SystemBarStyle.light(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT),
            navigationBarStyle=SystemBarStyle.light(android.graphics.Color.rgb(250,248,245),android.graphics.Color.rgb(250,248,245)))
        setContent {
            MaterialTheme(colorScheme=lightColorScheme(
                primary=Coral,onPrimary=Color.White,primaryContainer=Peach,onPrimaryContainer=Ink,
                secondary=Lilac,secondaryContainer=Lavender,background=Cream,onBackground=Ink,
                surface=Color.White,onSurface=Ink,onSurfaceVariant=Muted,outline=Line,
                surfaceVariant=Line,surfaceContainer=Cream)) { DailyApp() }
        }
    }
}

@Composable
private fun DailyApp(vm: DailyViewModel = viewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var goalSchedule by rememberSaveable { mutableStateOf(if(KoreanHolidays.isRestDay(koreaToday())) GoalSchedule.WEEKEND else GoalSchedule.WEEKDAY) }
    var adding by remember { mutableStateOf(false) }
    var profileOpen by rememberSaveable {mutableStateOf(false)}
    var editing by rememberSaveable { mutableStateOf(false) }
    var celebration by remember { mutableIntStateOf(0) }
    var notice by remember {mutableStateOf<String?>(null)}
    var noticeTrigger by remember {mutableIntStateOf(0)}
    LaunchedEffect(noticeTrigger) {
        if(notice!=null) {kotlinx.coroutines.delay(1800);notice=null}
    }
    var today by remember { mutableStateOf(koreaToday()) }
    val scroll = rememberScrollState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { while(true) { today=koreaToday(); kotlinx.coroutines.delay(30_000) } }
    LaunchedEffect(tab) { scroll.scrollTo(0) }
    LaunchedEffect(vm.error) {
        vm.error?.let { snackbar.showSnackbar(it,actionLabel="확인",duration=SnackbarDuration.Long); vm.clearError() }
    }
    Scaffold(containerColor=Cream,snackbarHost={ SnackbarHost(snackbar) },bottomBar={
        if(vm.pairId.isNotEmpty()) BottomMenu(tab) { tab=it }
    }) { padding ->
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(horizontal=if(tab==1&&vm.pairId.isNotEmpty())0.dp else if(tab==2&&vm.pairId.isNotEmpty())12.dp else 22.dp),
            verticalArrangement=Arrangement.spacedBy(if(tab!=0&&vm.pairId.isNotEmpty())8.dp else 22.dp)) {
            Spacer(Modifier.height(1.dp))
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(Peach,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center) {
                    Mark(Symbol.Heart,color=Coral,modifier=Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("쏘규 Daily 챌린지",color=Ink,fontSize=20.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.5).sp,modifier=Modifier.weight(1f))
                if(vm.pairId.isNotEmpty()) {
                    TextButton(onClick={profileOpen=true},contentPadding=PaddingValues(horizontal=6.dp)) {Text(vm.names[vm.uid].orEmpty(),fontSize=13.sp)}
                } else Text("WITH YOU",color=Lilac,fontSize=10.sp,fontWeight=FontWeight.SemiBold,letterSpacing=1.sp)
            }
            if(vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp),color=Coral,trackColor=Peach)
            when {
                !vm.configured && !vm.demo -> {
                    WelcomeArt("같이 시작하는\n우리의 좋은 습관","작은 실천 하나에도, 서로의 응원을 담아요.")
                    SoftCard {
                        Text("잠깐 둘러볼까요?",fontWeight=FontWeight.Bold,fontSize=18.sp)
                        Text("함께 목표를 세우고 하루를 기록하는 모습을 미리 만나보세요.",color=Muted,fontSize=14.sp,lineHeight=22.sp)
                        PrimaryAction("체험해보기",onClick={ vm.startDemo() })
                    }
                }
                vm.uid.isEmpty() -> LoginPage(vm)
                !vm.ready -> SoftCard {
                    Text("쏘규 Daily 챌린지를 불러오고 있어요",fontWeight=FontWeight.SemiBold)
                    Text("잠시만 기다려주세요.",color=Muted,fontSize=13.sp)
                    PrimaryAction("다시 불러오기",enabled=!vm.busy,onClick={vm.retryProfile()})
                    TextButton(onClick={vm.logout()},enabled=!vm.busy) { Text("로그아웃") }
                }
                vm.pairId.isEmpty() -> ConnectPage(vm)
                else -> {
                    if(vm.demo) StatusPill("체험 모드",Lilac,Lavender)
                    else if(vm.offline) StatusPill("연결을 기다리고 있어요 · 기록은 보관 중",Muted,Line)
                    when(tab) {
                        0 -> TodayPage(vm,today,vm.scheduleFor(today),onEdit={goalSchedule=vm.scheduleFor(today);editing=true},onCelebrate={celebration++},onMemoSaved={notice=it;noticeTrigger++})
                        1 -> HistoryPage(vm,today)
                        2,3 -> StatsPage(vm,today,onLogout={vm.logout();tab=0})
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Fireworks(celebration)
        AnimatedVisibility(visible=notice!=null,modifier=Modifier.align(Alignment.Center),
            enter=fadeIn()+scaleIn(initialScale=.92f),exit=fadeOut()+scaleOut(targetScale=.96f)) {
            Surface(shape=RoundedCornerShape(20.dp),color=Ink.copy(alpha=.94f),shadowElevation=8.dp) {
                Row(Modifier.padding(horizontal=20.dp,vertical=15.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    Mark(Symbol.Check,color=Mint,modifier=Modifier.size(20.dp))
                    Text(notice?:"메모를 저장했어요",color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Medium)
                }
            }
        }
        }
    }
    if(profileOpen&&vm.pairId.isNotEmpty()) ProfileDialog(vm,onDismiss={profileOpen=false})
    if(editing) GoalEditor(vm,today,goalSchedule,onSchedule={goalSchedule=it},onAdd={adding=true},onDismiss={editing=false})
    if(adding) {
        val ownGoals=vm.goals.filter {it.ownerId==vm.uid&&it.schedule==goalSchedule}
        var position by rememberSaveable {mutableIntStateOf(ownGoals.size)}
        var title by rememberSaveable { mutableStateOf("") }
        var schedule by rememberSaveable { mutableStateOf(goalSchedule) }
        var target by rememberSaveable { mutableStateOf<Int?>(null) }
        AlertDialog(onDismissRequest={adding=false},containerColor=Cream,shape=RoundedCornerShape(28.dp),
            icon={Mark(Symbol.Leaf,color=Sage,modifier=Modifier.size(28.dp))},
            title={Text("목표 추가",fontWeight=FontWeight.Bold,fontSize=22.sp)},
            text={Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Text("평일 또는 주말·공휴일 목표를 설정하세요.",color=Muted,fontSize=14.sp,lineHeight=22.sp)
                GoalSettings(schedule,target,!vm.busy,onSchedule={schedule=it},onTarget={target=it})
                OutlinedTextField(title,{if(it.length<=60)title=it},singleLine=true,
                    placeholder={Text("목표 이름",fontSize=13.sp)},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())

                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text("추가 위치: ${position.coerceAtMost(ownGoals.size)+1}번째",fontSize=13.sp,modifier=Modifier.weight(1f))
                    TextButton(onClick={position--},enabled=position>0&&!vm.busy){Text("↑")}
                    TextButton(onClick={position++},enabled=position<ownGoals.size&&!vm.busy){Text("↓")}
                }
            }},
            confirmButton={Button(onClick={vm.addGoal(title,position,schedule,target,onSaved={goalSchedule=schedule;adding=false})},enabled=title.isNotBlank()&&!vm.busy,shape=RoundedCornerShape(12.dp)) {Text("추가")}},
            dismissButton={TextButton(onClick={adding=false}){Text("취소")}})
    }

}

@Composable
private fun BottomMenu(selected: Int,onSelect:(Int)->Unit) {
    Surface(color=Color.White,shadowElevation=8.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=18.dp,vertical=10.dp),
            horizontalArrangement=Arrangement.SpaceEvenly) {
            listOf("오늘","기록","달성률").forEachIndexed {i,title ->
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable {onSelect(i)}.padding(vertical=4.dp),
                    horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    val bg by animateColorAsState(if(selected==i)Peach else Color.Transparent,label="navigation")
                    Box(Modifier.size(width=48.dp,height=29.dp).background(bg,RoundedCornerShape(11.dp)),contentAlignment=Alignment.Center) {
                        Mark(listOf(Symbol.Home,Symbol.Calendar,Symbol.Chart)[i],color=if(selected==i)Coral else Muted,modifier=Modifier.size(20.dp))
                    }
                    Text(title,color=if(selected==i)Coral else Muted,fontSize=11.sp,fontWeight=if(selected==i)FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun TodayPage(vm:DailyViewModel,today:LocalDate,schedule:GoalSchedule,onEdit:()->Unit,onCelebrate:()->Unit,onMemoSaved:(String)->Unit) {
    var owner by rememberSaveable { mutableIntStateOf(0) }
    val id=if(owner==0) vm.uid else vm.names.keys.firstOrNull {it!=vm.uid}.orEmpty()
    val list=vm.goals.filter {it.ownerId==id&&it.schedule==schedule&&it.start<=today&&(it.end==null||it.end>=today)}
    val done=list.count {g->vm.entries.any {it.goalId==g.id&&it.date==today&&it.done}}
    SoftCard(color=Peach.copy(alpha=.65f)) {
        Text(today.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일",Locale.KOREAN)),color=Ink,fontSize=19.sp,fontWeight=FontWeight.Bold)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("${list.size}개 중 ${done}개 완수",color=Coral,fontSize=16.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
            TextButton(onClick=onEdit,enabled=!vm.busy) {Text("목표 수정")}
        }
        LinearProgressIndicator(progress={if(list.isEmpty())0f else done.toFloat()/list.size},modifier=Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),color=Coral,trackColor=Color.White.copy(alpha=.8f),drawStopIndicator={})
    }

    OwnerSwitch(vm,owner){owner=it}
    if(list.isEmpty()) Text("오늘 등록된 목표가 없습니다.",color=Muted,fontSize=14.sp)
    list.forEach {goal -> GoalTile(goal,vm.entries.find {it.goalId==goal.id&&it.date==today}?.done,
        editable=id==vm.uid&&!vm.busy&&goal.scheduled(today),onRecord={value ->
            val previouslyRecorded=allGoalsRecorded(list,vm.entries,today)
            vm.record(goal,today,value,onSaved={
                if(value!=null&&!previouslyRecorded&&allGoalsRecorded(list,vm.entries,today)) onCelebrate()
            })
        })}
    val saved=vm.memos.firstOrNull {it.ownerId==vm.uid&&it.date==today}?.text.orEmpty()
    var draft by rememberSaveable(vm.uid,today.toString(),stateSaver=Saver<MemoDraft,List<String>>(
        save={listOf(it.text,it.baseline)},restore={MemoDraft(it[0],it[1])}
    )) {mutableStateOf(MemoDraft(vm.memoDraft(today)?:saved,saved))}
    LaunchedEffect(saved) { draft=draft.remote(saved) }
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text("오늘 메모",fontSize=14.sp,fontWeight=FontWeight.SemiBold)
        OutlinedTextField(draft.text,{if(it.length<=2000) {draft=draft.copy(text=it);vm.editMemoDraft(today,it)}},placeholder={Text("오늘의 메모")},minLines=2,maxLines=5,modifier=Modifier.fillMaxWidth())
        TextButton(onClick={
            val submitted = draft.text
            vm.saveMemo(today,submitted,onSaved={draft=draft.confirmed(submitted);onMemoSaved(if(submitted.isBlank())"메모를 삭제했어요" else "메모를 저장했어요")})
        },enabled=!vm.busy&&draft.text.trim()!=saved,modifier=Modifier.align(Alignment.End)) {Text("메모 저장")}
        vm.memoStatus?.takeUnless {it=="메모를 저장했어요."||it=="메모를 삭제했어요."}?.let { Text(it,fontSize=12.sp,color=Muted) }
    }

}

@Composable
private fun GoalEditor(vm:DailyViewModel,today:LocalDate,schedule:GoalSchedule,onSchedule:(GoalSchedule)->Unit,onAdd:()->Unit,onDismiss:()->Unit) {
    var deleting by remember {mutableStateOf<Goal?>(null)}
    var reordering by remember {mutableStateOf(false)}
    val goals=vm.goals.filter {it.ownerId==vm.uid&&it.schedule==schedule}
    val maxHeight=(LocalConfiguration.current.screenHeightDp*.86f).dp
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(modifier=Modifier.widthIn(max=440.dp).fillMaxWidth(.94f),shape=RoundedCornerShape(22.dp),color=Cream) {
            Column(Modifier.heightIn(max=maxHeight).padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("목표 수정",fontSize=20.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                    TextButton(onClick=onDismiss,contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp),modifier=Modifier.height(32.dp)) {Text("닫기")}
                }
                Column(Modifier.weight(1f,fill=false).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    val currentDaySchedule=vm.scheduleFor(today)
                    val currentLabel=if(currentDaySchedule==GoalSchedule.WEEKDAY)"평일" else "주말"
                    val otherSchedule=if(currentDaySchedule==GoalSchedule.WEEKDAY)GoalSchedule.WEEKEND else GoalSchedule.WEEKDAY
                    val otherLabel=if(otherSchedule==GoalSchedule.WEEKDAY)"평일" else "주말"
                    Column(verticalArrangement=Arrangement.spacedBy(2.dp)) {
                        Text("오늘은 \"${currentLabel}\"인데 \"${otherLabel}\"로 바꿀까요?",fontSize=12.sp,color=Muted)
                        TextButton(onClick={vm.setDaySchedule(today,otherSchedule);onSchedule(otherSchedule)},enabled=!vm.busy,
                            contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp),modifier=Modifier.height(32.dp)) {Text("${otherLabel}로 바꾸기",fontSize=12.sp)}
                    }
                    Segments(GoalSchedule.entries.map {it.label},schedule.ordinal,verticalPadding=8.dp) {onSchedule(GoalSchedule.entries[it])}
                    if(goals.isEmpty()) Text("등록된 목표가 없습니다.",color=Muted,modifier=Modifier.padding(vertical=8.dp))
                    goals.forEach {goal -> key(goal.id) {
                        var title by rememberSaveable(goal.title) {mutableStateOf(goal.title)}

                        var target by rememberSaveable(goal.weeklyTarget) {mutableStateOf(goal.weeklyTarget)}
                        Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(14.dp))
                            .border(1.dp,Line,RoundedCornerShape(14.dp)).padding(8.dp),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                            OutlinedTextField(title,{if(it.length<=60)title=it},placeholder={Text("목표 이름")},singleLine=true,
                                enabled=!vm.busy,shape=RoundedCornerShape(10.dp),modifier=Modifier.fillMaxWidth())
                            GoalSettings(goal.schedule,target,!vm.busy,onSchedule={},onTarget={target=it},showSchedule=false)
                            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.End) {
                                TextButton(onClick={vm.updateGoal(goal,title,goal.schedule,target)},
                                    enabled=!vm.busy&&title.isNotBlank()&&(title.trim()!=goal.title||target!=goal.weeklyTarget),
                                    contentPadding=PaddingValues(horizontal=10.dp,vertical=0.dp),modifier=Modifier.height(32.dp)) {Text("저장",fontSize=12.sp)}
                                TextButton(onClick={deleting=goal},enabled=!vm.busy,contentPadding=PaddingValues(horizontal=10.dp,vertical=0.dp),modifier=Modifier.height(32.dp)) {Text("삭제",fontSize=12.sp)}
                            }
                        }
                    }}
                    OutlinedButton(onClick=onAdd,enabled=!vm.busy,modifier=Modifier.fillMaxWidth().height(40.dp),contentPadding=PaddingValues(0.dp)) {Text("목표 추가")}
                    OutlinedButton(onClick={reordering=true},enabled=!vm.busy&&goals.size>1,modifier=Modifier.fillMaxWidth().height(40.dp),contentPadding=PaddingValues(0.dp)) {Text("순서변경")}
                }
            }
        }
    }
    if(reordering) GoalOrderDialog(vm,schedule,onDismiss={reordering=false})
    deleting?.let {goal ->
        AlertDialog(onDismissRequest={deleting=null},title={Text("목표 삭제")},
            text={Text("‘${goal.title}’ 목표와 해당 목표의 기록을 삭제합니다.")},
            confirmButton={TextButton(onClick={vm.deleteGoal(goal);deleting=null},enabled=!vm.busy) {Text("삭제")}},
            dismissButton={TextButton(onClick={deleting=null}) {Text("취소")}})
    }
}
@Composable
internal fun EmptyGoals(title:String,subtitle:String,onAdd:(()->Unit)?=null) {
    SoftCard {
        Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(58.dp).background(Mint,RoundedCornerShape(20.dp)),contentAlignment=Alignment.Center) {Mark(Symbol.Leaf,color=Sage,modifier=Modifier.size(27.dp))}
            Text(title,fontWeight=FontWeight.SemiBold,fontSize=16.sp)
            Text(subtitle,color=Muted,fontSize=13.sp,lineHeight=21.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            onAdd?.let { TextButton(onClick=it) {Text("나의 첫 목표 만들기",color=Coral,fontSize=13.sp)} }
        }
    }
}

@Composable
internal fun GoalTile(goal:Goal,done:Boolean?,editable:Boolean,onRecord:(Boolean?)->Unit,onEnd:(()->Unit)?=null) {
    var menu by remember {mutableStateOf(false)}
    SoftCard {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(42.dp).background(if(done==true)Mint else Lavender,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center) {
                Mark(if(done==true)Symbol.Check else goalSymbol(goal.title),color=if(done==true)Sage else Lilac)
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Text(goal.title,fontSize=15.sp,fontWeight=FontWeight.SemiBold,lineHeight=22.sp)
                Text(listOf(goal.schedule.label,goal.frequencyLabel).filter {it.isNotBlank()}.joinToString(" · "),fontSize=11.sp,color=Muted)
            }
            if(editable&&(done!=null||onEnd!=null)) Box {
                IconButton(onClick={menu=true},modifier=Modifier.size(32.dp)) {Mark(Symbol.More,color=Muted,description="목표 메뉴")}
                DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                    if(done!=null) DropdownMenuItem(text={Text("기록 취소")},onClick={menu=false;onRecord(null)})
                    onEnd?.let {DropdownMenuItem(text={Text("목표 종료")},onClick={menu=false;it()})}
                }
            }
        }
        if(editable) Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            RecordButton("완료했어요",done==true,Sage,Mint,Symbol.Check,Modifier.weight(1f)){onRecord(true)}
            RecordButton("미완료",done==false,Muted,Line,null,Modifier.weight(1f)){onRecord(false)}
        } else StatusPill(when(done){true->"완료했어요";false->"미완료";null->"아직 미기록"},
            if(done==true)Sage else Muted,if(done==true)Mint else Line)
    }
}
@Composable
private fun RecordButton(text:String,selected:Boolean,color:Color,bg:Color,symbol:Symbol?,modifier:Modifier,onClick:()->Unit) {
    Surface(modifier.heightIn(min=44.dp).clip(RoundedCornerShape(13.dp)).clickable(onClick=onClick),
        shape=RoundedCornerShape(13.dp),color=if(selected)bg else Cream,
        border=androidx.compose.foundation.BorderStroke(1.dp,if(selected)color.copy(alpha=.2f)else Line)) {
        Row(Modifier.padding(horizontal=6.dp,vertical=11.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            symbol?.let {Mark(it,color=if(selected)color else Muted,modifier=Modifier.size(15.dp));Spacer(Modifier.width(5.dp))}
            Text(text,color=if(selected)color else Muted,fontSize=12.sp,fontWeight=if(selected)FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun GoalSettings(schedule:GoalSchedule,target:Int?,enabled:Boolean,onSchedule:(GoalSchedule)->Unit,onTarget:(Int?)->Unit,showSchedule:Boolean=true) {
    var expanded by remember {mutableStateOf(false)}
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        if(showSchedule) Row(verticalAlignment=Alignment.CenterVertically) {
            Text("목표 분류",fontSize=12.sp,modifier=Modifier.weight(1f))
            GoalSchedulePicker(schedule,enabled,onSchedule)
        }
        TextButton(onClick={expanded=!expanded},enabled=enabled,modifier=Modifier.fillMaxWidth().height(32.dp),
            contentPadding=PaddingValues(horizontal=4.dp,vertical=0.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                Text("주당 횟수 설정",fontSize=11.sp,color=Muted)
                Text("${target?.let {"주 ${it}회"}?:"OFF"} ${if(expanded)"▴" else "▾"}",fontSize=11.sp,color=if(target==null)Muted else Coral)
            }
        }
        if(expanded) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                listOf<Int?>(1,2,3,4,null).forEach {value ->
                    val selected=target==value
                    Box(Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                        .background(if(selected)Coral else Color.White.copy(alpha=.7f))
                        .selectable(selected=selected,enabled=enabled,role=androidx.compose.ui.semantics.Role.RadioButton,onClick={onTarget(value);if(value==null)expanded=false})
                        .semantics {contentDescription=if(value==null)"주당 횟수 비활성화" else "주당 ${value}회"},contentAlignment=Alignment.Center) {
                        Text(value?.toString()?:"X",fontSize=12.sp,color=if(selected)Color.White else Muted,fontWeight=FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
@Composable
private fun GoalSchedulePicker(schedule:GoalSchedule,enabled:Boolean,onSelect:(GoalSchedule)->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Box {
        TextButton(onClick={expanded=true},enabled=enabled,contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp),modifier=Modifier.height(36.dp)) {
            Text("${schedule.label} ▾",fontSize=12.sp)
        }
        DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) {
            GoalSchedule.entries.forEach {value->DropdownMenuItem(text={Text(value.label)},onClick={expanded=false;onSelect(value)})}
        }
    }
}