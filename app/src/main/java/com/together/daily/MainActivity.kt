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
    var dateText by rememberSaveable { mutableStateOf(koreaToday().toString()) }
    var adding by remember { mutableStateOf(false) }
    var ending by remember { mutableStateOf<Goal?>(null) }
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
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(horizontal=22.dp),
            verticalArrangement=Arrangement.spacedBy(22.dp)) {
            Spacer(Modifier.height(1.dp))
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(Peach,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center) {
                    Mark(Symbol.Heart,color=Coral,modifier=Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("우리의 하루",color=Ink,fontSize=20.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.5).sp,modifier=Modifier.weight(1f))
                if(vm.pairId.isNotEmpty()) {
                    Box(Modifier.clip(CircleShape).clickable { tab=3 }) { Avatar(vm.names[vm.uid].orEmpty()) }
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
                    Text("우리의 하루를 불러오고 있어요",fontWeight=FontWeight.SemiBold)
                    Text("잠시만 기다려주세요.",color=Muted,fontSize=13.sp)
                    PrimaryAction("다시 불러오기",enabled=!vm.busy,onClick={vm.retryProfile()})
                    TextButton(onClick={vm.logout()},enabled=!vm.busy) { Text("로그아웃") }
                }
                vm.pairId.isEmpty() -> ConnectPage(vm)
                else -> {
                    if(vm.demo) StatusPill("체험 모드",Lilac,Lavender)
                    else if(vm.offline) StatusPill("연결을 기다리고 있어요 · 기록은 보관 중",Muted,Line)
                    when(tab) {
                        0 -> TodayPage(vm,today,onAdd={adding=true},onEnd={ending=it},
                            onDate={dateText=it.toString();tab=1},onInvite={tab=3})
                        1 -> HistoryPage(vm,LocalDate.parse(dateText),today,onDate={dateText=it.toString()})
                        2 -> StatsPage(vm,today)
                        3 -> CouplePage(vm,onLogout={vm.logout();tab=0})
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
    if(adding) {
        var title by rememberSaveable { mutableStateOf("") }
        AlertDialog(onDismissRequest={adding=false},containerColor=Cream,shape=RoundedCornerShape(28.dp),
            icon={Mark(Symbol.Leaf,color=Sage,modifier=Modifier.size(28.dp))},
            title={Text("새로운 작은 약속",fontWeight=FontWeight.Bold,fontSize=22.sp)},
            text={Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Text("거창하지 않아도 좋아요.\n오늘부터 매일 해볼 일을 정해보세요.",color=Muted,fontSize=14.sp,lineHeight=22.sp)
                OutlinedTextField(title,{if(it.length<=60)title=it},singleLine=true,
                    placeholder={Text("예: 함께 생각하며 30분 걷기",fontSize=13.sp)},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
                Text("이런 약속은 어때요?",color=Muted,fontSize=12.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    listOf("30분 산책","책 10쪽 읽기").forEach { suggestion ->
                        SuggestionChip(onClick={title=suggestion},label={Text(suggestion,fontSize=12.sp)})
                    }
                }
            }},
            confirmButton={Button(onClick={vm.addGoal(title);adding=false},enabled=title.isNotBlank()&&!vm.busy,shape=RoundedCornerShape(12.dp)) {Text("약속 만들기")}},
            dismissButton={TextButton(onClick={adding=false}){Text("다음에")}})
    }
    ending?.let {goal ->
        AlertDialog(onDismissRequest={ending=null},containerColor=Cream,shape=RoundedCornerShape(28.dp),
            title={Text("이 약속을 마무리할까요?",fontSize=21.sp,fontWeight=FontWeight.Bold)},
            text={Text("‘${goal.title}’은 오늘까지 기록할 수 있어요. 함께 쌓은 지난 기록은 그대로 남아요.",color=Muted,lineHeight=23.sp)},
            confirmButton={TextButton(onClick={vm.archive(goal);ending=null}){Text("목표 종료")}},
            dismissButton={TextButton(onClick={ending=null}){Text("계속할래요")}})
    }
}

@Composable
private fun BottomMenu(selected: Int,onSelect:(Int)->Unit) {
    Surface(color=Color.White,shadowElevation=8.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=18.dp,vertical=10.dp),
            horizontalArrangement=Arrangement.SpaceEvenly) {
            listOf("오늘","기록","달성률","우리").forEachIndexed {i,title ->
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable {onSelect(i)}.padding(vertical=4.dp),
                    horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    val bg by animateColorAsState(if(selected==i)Peach else Color.Transparent,label="navigation")
                    Box(Modifier.size(width=48.dp,height=29.dp).background(bg,RoundedCornerShape(11.dp)),contentAlignment=Alignment.Center) {
                        Mark(listOf(Symbol.Home,Symbol.Calendar,Symbol.Chart,Symbol.People)[i],color=if(selected==i)Coral else Muted,modifier=Modifier.size(20.dp))
                    }
                    Text(title,color=if(selected==i)Coral else Muted,fontSize=11.sp,fontWeight=if(selected==i)FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun TodayPage(vm:DailyViewModel,today:LocalDate,onAdd:()->Unit,onEnd:(Goal)->Unit,onDate:(LocalDate)->Unit,onInvite:()->Unit) {
    var owner by rememberSaveable { mutableIntStateOf(0) }
    val own=vm.goals.filter {it.ownerId==vm.uid&&it.scheduled(today)}
    val done=own.count {g->vm.entries.any {it.goalId==g.id&&it.date==today&&it.done}}
    SoftCard(color=Peach.copy(alpha=.65f)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(9.dp)) {
                Text(today.format(DateTimeFormatter.ofPattern("M월 d일 E요일",Locale.KOREAN)),color=Coral,fontSize=12.sp,fontWeight=FontWeight.Medium)
                Text("작은 약속이\n큰 행복이 돼요",fontSize=25.sp,lineHeight=34.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.7).sp)
                Text("오늘도 서로의 가장 좋은 응원",color=Muted,fontSize=11.sp)
            }
            TogetherArt(Modifier.size(112.dp))
        }
        HorizontalDivider(color=Coral.copy(alpha=.12f))
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("오늘의 내 실천",color=Muted,fontSize=12.sp,modifier=Modifier.weight(1f))
            Text("$done",color=Coral,fontSize=21.sp,fontWeight=FontWeight.Bold)
            Text(" / ${own.size} 완료",color=Muted,fontSize=12.sp)
        }
        LinearProgressIndicator(progress={if(own.isEmpty())0f else done.toFloat()/own.size},modifier=Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),color=Coral,trackColor=Color.White.copy(alpha=.8f),drawStopIndicator={})
    }
    WeekRibbon(vm,today,onDate)
    Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("오늘의 작은 약속",fontSize=18.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
            TextButton(onClick=onAdd,enabled=!vm.busy,contentPadding=PaddingValues(horizontal=6.dp)) {
                Mark(Symbol.Plus,color=Coral,modifier=Modifier.size(16.dp)); Spacer(Modifier.width(4.dp));Text("목표 추가",fontSize=12.sp)
            }
        }
        val id=OwnerSwitch(vm,owner){owner=it}
        val list=vm.goals.filter {it.ownerId==id&&it.scheduled(today)}
        if(list.isEmpty()) EmptyGoals(if(id==vm.uid)"첫 약속을 만들어볼까요?" else "짝꿍의 약속을 기다리는 중",
            if(id==vm.uid)"가벼운 산책, 책 한 페이지.\n작게 시작해도 충분히 멋져요." else "서로의 속도로, 함께 시작해요.",
            if(id==vm.uid) onAdd else null)
        list.forEach {goal -> GoalTile(goal,vm.entries.find {it.goalId==goal.id&&it.date==today}?.done,
            editable=id==vm.uid&&!vm.busy,onRecord={vm.record(goal,today,it)},onEnd=if(id==vm.uid&&goal.end==null)({onEnd(goal)})else null)}
    }
    if(vm.names.size==1) Surface(color=Lavender,shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth().clickable(onClick=onInvite)) {
        Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Mark(Symbol.People,color=Lilac)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text("좋은 습관, 이제 둘이 함께",color=Ink,fontSize=14.sp,fontWeight=FontWeight.SemiBold)
                Text("짝꿍에게 초대 코드를 보내주세요",color=Lilac,fontSize=11.sp)
            }
            Mark(Symbol.Next,color=Lilac,modifier=Modifier.size(16.dp))
        }
    }
}

@Composable
private fun WeekRibbon(vm:DailyViewModel,today:LocalDate,onDate:(LocalDate)->Unit) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        (0L..6L).forEach {offset ->
            val day=weekStart(today).plusDays(offset)
            val chosen=day==today
            val done=vm.entries.any {it.date==day&&it.done}
            Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(if(chosen)Ink else Color.White)
                .clickable(enabled=day<=today){onDate(day)}.padding(vertical=12.dp),
                horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                Text(day.format(DateTimeFormatter.ofPattern("E",Locale.KOREAN)),fontSize=10.sp,color=if(chosen)Color.White.copy(alpha=.7f)else Muted)
                Text(day.dayOfMonth.toString(),fontSize=15.sp,fontWeight=FontWeight.SemiBold,color=if(chosen)Color.White else if(day>today)Muted.copy(alpha=.4f)else Ink)
                Box(Modifier.size(4.dp).background(if(done)if(chosen)Color(0xFFF0B8A5)else Sage else if(chosen)Color.White.copy(alpha=.25f)else Line,CircleShape))
            }
        }
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
                Text(if(goal.end==null)"매일 조금씩, 나를 위한 시간" else "차곡차곡 쌓아온 약속",fontSize=10.sp,color=Muted)
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
