package com.together.daily

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


@Composable
internal fun GoalOrderDialog(vm:DailyViewModel,onDismiss:()->Unit) {
    var ordered by remember {mutableStateOf(vm.goals.filter {it.ownerId==vm.uid})}
    val listState=rememberLazyListState()
    var draggedId by remember {mutableStateOf<String?>(null)}
    var draggedTop by remember {mutableFloatStateOf(0f)}
    var draggedHeight by remember {mutableIntStateOf(0)}
    fun moveAtCenter() {
        val id=draggedId?:return
        val center=draggedTop+draggedHeight/2f
        val target=listState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key!=id&&center>=it.offset&&center<=it.offset+it.size
        }?:return
        val from=ordered.indexOfFirst {it.id==id}
        val to=ordered.indexOfFirst {it.id==target.key}
        if(from>=0&&to>=0) ordered=ordered.toMutableList().apply {add(to,removeAt(from))}
    }
    val maxHeight=(LocalConfiguration.current.screenHeightDp*.84f).dp
    Dialog(onDismissRequest={if(!vm.busy)onDismiss()},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(modifier=Modifier.fillMaxWidth(.98f),shape=RoundedCornerShape(22.dp),color=Cream) {
            Column(Modifier.heightIn(max=maxHeight).padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("${schedule.label} 목표 순서변경",style=MaterialTheme.typography.titleLarge)
                Text("목표를 끌어서 순서를 바꾸세요. 양옆 여백을 쓸어 목록을 스크롤할 수 있어요.",color=Muted,style=MaterialTheme.typography.bodySmall)
                BoxWithConstraints(Modifier.weight(1f,fill=false).fillMaxWidth()) {
                    val cardWidth=minOf(280.dp,(maxWidth-64.dp).coerceAtLeast(120.dp))
                    LazyColumn(state=listState,modifier=Modifier.fillMaxWidth().heightIn(max=maxHeight-140.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(ordered,key={_,goal->goal.id}) {index,goal->
                            val active=draggedId==goal.id
                            val offset=listState.layoutInfo.visibleItemsInfo.firstOrNull {it.key==goal.id}?.offset?:0
                            Box(Modifier.fillMaxWidth().zIndex(if(active)1f else 0f),contentAlignment=Alignment.Center) {
                                Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,
                                    border=BorderStroke(1.dp,if(active)Coral else Line),
                                    modifier=Modifier.width(cardWidth)
                                        .graphicsLayer {translationY=if(active)draggedTop-offset else 0f}
                                        .pointerInput(goal.id,vm.busy) {
                                            if(!vm.busy) detectDragGestures(
                                                onDragStart={
                                                    listState.layoutInfo.visibleItemsInfo.firstOrNull {it.key==goal.id}?.let {
                                                        draggedId=goal.id;draggedTop=it.offset.toFloat();draggedHeight=it.size
                                                    }
                                                },
                                                onDragEnd={draggedId=null},onDragCancel={draggedId=null},
                                                onDrag={change,amount->change.consume();draggedTop+=amount.y;moveAtCenter()})
                                        }) {
                                    Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                        Text("${index+1}",color=Coral)
                                        Text(goal.title,modifier=Modifier.weight(1f))
                                        Text("↕",color=Muted)
                                    }
                                }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                    TextButton(onClick=onDismiss,enabled=!vm.busy) {Text("취소")}
                    TextButton(onClick={vm.reorderGoals(ordered.map {it.id},onSaved=onDismiss)},enabled=!vm.busy&&draggedId==null) {Text("순서 저장")}
                }
            }
        }
    }
}