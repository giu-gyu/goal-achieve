package com.together.daily

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
internal fun GoalOrderDialog(vm:DailyViewModel,onDismiss:()->Unit) {
    var ordered by remember {mutableStateOf(vm.goals.filter {it.ownerId==vm.uid})}
    val listState=rememberLazyListState()
    val scope=rememberCoroutineScope()
    var draggedId by remember {mutableStateOf<String?>(null)}
    var draggedTop by remember {mutableFloatStateOf(0f)}
    var draggedHeight by remember {mutableIntStateOf(0)}
    var grabOffset by remember {mutableFloatStateOf(0f)}
    val density=LocalDensity.current
    val edgeSize=with(density){64.dp.toPx()}
    val maxSpeed=with(density){640.dp.toPx()}
    fun moveAtCenter() {
        val id=draggedId?:return
        val from=ordered.indexOfFirst {it.id==id}
        if(from<0)return
        val center=draggedTop+draggedHeight/2f
        val target=listState.layoutInfo.visibleItemsInfo.lastOrNull {
            (it.index>from && center>it.offset+it.size/2f) ||
                (it.index<from && center<it.offset+it.size/2f)
        }?:return
        val to=ordered.indexOfFirst {it.id==target.key}
        if(to<0)return
        // Prevent LazyColumn's keyed first-item anchor from scrolling after a move.
        listState.requestScrollToItem(listState.firstVisibleItemIndex,listState.firstVisibleItemScrollOffset)
        ordered=ordered.toMutableList().apply {add(to,removeAt(from))}
    }
    LaunchedEffect(draggedId) {
        if(draggedId==null)return@LaunchedEffect
        var previous=withFrameNanos {it}
        while(isActive) {
            val now=withFrameNanos {it}
            val seconds=((now-previous)/1_000_000_000f).coerceAtMost(.05f)
            previous=now
            val layout=listState.layoutInfo
            val speed=dragScrollFraction(draggedTop,draggedHeight.toFloat(),layout.viewportStartOffset.toFloat(),layout.viewportEndOffset.toFloat(),edgeSize)*maxSpeed
            if(speed!=0f)listState.scrollBy(speed*seconds)
            moveAtCenter()
        }
    }
    val maxHeight=(LocalConfiguration.current.screenHeightDp*.84f).dp
    Dialog(onDismissRequest={if(!vm.busy)onDismiss()},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(modifier=Modifier.fillMaxWidth(.98f),shape=RoundedCornerShape(22.dp),color=Cream) {
            Column(Modifier.heightIn(max=maxHeight).padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("목표 순서변경",style=MaterialTheme.typography.titleLarge)
                Text("목표를 끌어서 순서를 바꾸세요. 위·아래 가장자리에 누르고 있으면 목록이 자동으로 움직여요.",color=Muted,style=MaterialTheme.typography.bodySmall)
                BoxWithConstraints(Modifier.weight(1f,fill=false).fillMaxWidth()) {
                    val cardWidth=minOf(280.dp,(maxWidth-64.dp).coerceAtLeast(120.dp))
                    val cardWidthPx=with(density){cardWidth.toPx()}
                    Box(Modifier.fillMaxWidth().pointerInput(vm.busy) {
                        if(!vm.busy)detectDragGestures(
                            onDragStart={position ->
                                val left=(size.width-cardWidthPx)/2f
                                if(position.x in left..(left+cardWidthPx)) {
                                    listState.layoutInfo.visibleItemsInfo.firstOrNull {position.y>=it.offset && position.y<it.offset+it.size}?.let {
                                        draggedId=it.key as String
                                        draggedTop=it.offset.toFloat()
                                        draggedHeight=it.size
                                        grabOffset=position.y-draggedTop
                                    }
                                }
                            },
                            onDragEnd={draggedId=null},onDragCancel={draggedId=null},
                            onDrag={change,amount ->
                                if(draggedId!=null) {
                                    change.consume()
                                    draggedTop=change.position.y-grabOffset
                                } else {
                                    change.consume()
                                    scope.launch {listState.scrollBy(-amount.y)}
                                }
                            })
                    }) {
                        LazyColumn(state=listState,userScrollEnabled=false,modifier=Modifier.fillMaxWidth().heightIn(max=maxHeight-140.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(ordered,key={_,goal->goal.id}) {index,goal->
                                Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
                                    OrderCard(index,goal.title,false,Modifier.width(cardWidth).graphicsLayer {alpha=if(draggedId==goal.id)0f else 1f})
                                }
                            }
                        }
                        // The dragged card and its gesture stay outside recycled/reordered rows.
                        draggedId?.let {id ->
                            val index=ordered.indexOfFirst {it.id==id}
                            if(index>=0) OrderCard(index,ordered[index].title,true,Modifier.align(Alignment.TopCenter).width(cardWidth).graphicsLayer {translationY=draggedTop})
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

@Composable
private fun OrderCard(index:Int,title:String,active:Boolean,modifier:Modifier) {
    Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,
        border=BorderStroke(1.dp,if(active)Coral else Line),modifier=modifier) {
        Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Text((index+1).toString(),color=Coral)
            Text(title,modifier=Modifier.weight(1f))
            Text("↕",color=Muted)
        }
    }
}

internal fun dragScrollFraction(top:Float,height:Float,start:Float,end:Float,edge:Float):Float {
    val zone=minOf(edge,(end-start)/3f)
    if(zone<=0f)return 0f
    val above=((start+zone-top)/zone).coerceIn(0f,1f)
    val below=((top+height-(end-zone))/zone).coerceIn(0f,1f)
    return if(above>below)-above else below
}
