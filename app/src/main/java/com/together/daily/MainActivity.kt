package com.together.daily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var adding by remember { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var celebration by remember { mutableIntStateOf(0) }
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
                    Box(Modifier.clip(CircleShape).clickable { tab=2 }) { Avatar(vm.names[vm.uid].orEmpty()) }
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
                        0 -> TodayPage(vm,today,onEdit={editing=true},onCelebrate={celebration++})
                        1 -> HistoryPage(vm,today)
                        2,3 -> StatsPage(vm,today,onLogout={vm.logout();tab=0})
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Fireworks(celebration)
        }
    }
    if(editing) GoalEditor(vm,onAdd={adding=true},onDismiss={editing=false})
    if(adding) {
        var title by rememberSaveable { mutableStateOf("") }
        AlertDialog(onDismissRequest={adding=false},containerColor=Cream,shape=RoundedCornerShape(28.dp),
            icon={Mark(Symbol.Leaf,color=Sage,modifier=Modifier.size(28.dp))},
            title={Text("목표 추가",fontWeight=FontWeight.Bold,fontSize=22.sp)},
            text={Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Text("오늘부터 매일 수행할 목표를 입력하세요.",color=Muted,fontSize=14.sp,lineHeight=22.sp)
                OutlinedTextField(title,{if(it.length<=60)title=it},singleLine=true,
                    placeholder={Text("예: 함께 생각하며 30분 걷기",fontSize=13.sp)},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
                Text("예시",color=Muted,fontSize=12.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    listOf("30분 산책","책 10쪽 읽기").forEach { suggestion ->
                        SuggestionChip(onClick={title=suggestion},label={Text(suggestion,fontSize=12.sp)})
                    }
                }
            }},
            confirmButton={Button(onClick={vm.addGoal(title);adding=false},enabled=title.isNotBlank()&&!vm.busy,shape=RoundedCornerShape(12.dp)) {Text("추가")}},
            dismissButton={TextButton(onClick={adding=false}){Text("다음에")}})
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
private fun TodayPage(vm:DailyViewModel,today:LocalDate,onEdit:()->Unit,onCelebrate:()->Unit) {
    var owner by rememberSaveable { mutableIntStateOf(0) }
    val id=if(owner==0) vm.uid else vm.names.keys.firstOrNull {it!=vm.uid}.orEmpty()
    val list=vm.goals.filter {it.ownerId==id&&it.scheduled(today)}
    val done=list.count {g->vm.entries.any {it.goalId==g.id&&it.date==today&&it.done}}
    var pendingCelebration by remember(id,today) {mutableStateOf<String?>(null)}
    LaunchedEffect(done,pendingCelebration,vm.busy,vm.error) {
        if(pendingCelebration!=null && list.isNotEmpty() && done==list.size && vm.error==null) {
            pendingCelebration=null
            onCelebrate()
        } else if(vm.error!=null) pendingCelebration=null
    }
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
        editable=id==vm.uid&&!vm.busy,onRecord={value ->
            if(value==true&&vm.entries.none {it.goalId==goal.id&&it.date==today&&it.done}) pendingCelebration=goal.id
            else pendingCelebration=null
            vm.record(goal,today,value)
        })}
    val saved=vm.memos.firstOrNull {it.ownerId==vm.uid&&it.date==today}?.text.orEmpty()
    var memo by rememberSaveable(vm.uid,today.toString(),saved) {mutableStateOf(saved)}
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text("오늘 메모",fontSize=14.sp,fontWeight=FontWeight.SemiBold)
        OutlinedTextField(memo,{if(it.length<=2000)memo=it},placeholder={Text("오늘의 메모")},minLines=2,maxLines=5,modifier=Modifier.fillMaxWidth())
        TextButton(onClick={vm.saveMemo(today,memo)},enabled=!vm.busy&&memo.trim()!=saved,modifier=Modifier.align(Alignment.End)) {Text("메모 저장")}
    }

}

@Composable
private fun GoalEditor(vm:DailyViewModel,onAdd:()->Unit,onDismiss:()->Unit) {
    var deleting by remember {mutableStateOf<Goal?>(null)}
    AlertDialog(onDismissRequest=onDismiss,containerColor=Cream,
        title={Text("목표 수정",fontWeight=FontWeight.Bold)},
        text={Column(Modifier.fillMaxWidth().heightIn(max=450.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            val goals=vm.goals.filter {it.ownerId==vm.uid}
            if(goals.isEmpty()) Text("등록된 목표가 없습니다.",color=Muted)
            goals.forEach {goal -> key(goal.id) {
                var title by rememberSaveable(goal.title) {mutableStateOf(goal.title)}
                Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(title,{if(it.length<=60)title=it},label={Text("목표 명칭")},singleLine=true,enabled=!vm.busy,modifier=Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick={vm.renameGoal(goal,title)},enabled=!vm.busy&&title.isNotBlank()&&title.trim()!=goal.title) {Text("저장")}
                        TextButton(onClick={deleting=goal},enabled=!vm.busy) {Text("삭제")}
                    }
                }
            }}
            OutlinedButton(onClick=onAdd,enabled=!vm.busy,modifier=Modifier.fillMaxWidth()) {Text("목표 추가")}
        }},
        confirmButton={TextButton(onClick=onDismiss) {Text("닫기")}})
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
            Box(Modifier.size(42.dp).background(if(done==true)Mint else Lavender.copy(alpha=.65f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center) {
                Mark(if(done==true)Symbol.Check else goalSymbol(goal.title),color=if(done==true)Sage else Lilac)
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Text(goal.title,fontSize=15.sp,fontWeight=FontWeight.SemiBold,lineHeight=22.sp)
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
