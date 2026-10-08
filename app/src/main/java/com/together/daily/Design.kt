package com.together.daily

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Coral = Color(0xFFC96770)
internal val Peach = Color(0xFFF9E8E2)
internal val Cream = Color(0xFFFAF8F5)
internal val Ink = Color(0xFF36313C)
internal val Muted = Color(0xFF746D78)
internal val Lavender = Color(0xFFECE8F4)
internal val Lilac = Color(0xFF786789)
internal val Sage = Color(0xFF466959)
internal val Mint = Color(0xFFE9F1EB)
internal val Line = Color(0xFFEEE9E5)

internal enum class Symbol { Heart, Home, Calendar, Chart, People, Check, Plus, Back, Next, More, Share, Copy, Book, Drop, Leaf }

@Composable
internal fun Mark(symbol: Symbol, modifier: Modifier = Modifier, color: Color = Ink, description: String? = null) {
    Canvas(modifier.size(22.dp).semantics { if (description != null) contentDescription = description }) {
        val scale = size.minDimension / 24f
        withTransform({ scale(scale, scale, Offset.Zero) }) {
            val stroke = Stroke(1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            fun line(x: Float, y: Float, a: Float, b: Float) = drawLine(color, Offset(x,y), Offset(a,b), 1.7f, StrokeCap.Round)
            fun path(block: Path.() -> Unit) = drawPath(Path().apply(block), color, style = stroke)
            when (symbol) {
                Symbol.Heart -> path { moveTo(12f,20f); cubicTo(9f,17f,3f,13f,3f,8f); cubicTo(3f,3f,9f,2f,12f,7f); cubicTo(15f,2f,21f,3f,21f,8f); cubicTo(21f,13f,15f,17f,12f,20f); close() }
                Symbol.Home -> { path { moveTo(3f,10f); lineTo(12f,3f); lineTo(21f,10f); moveTo(5f,9f); lineTo(5f,21f); lineTo(10f,21f); lineTo(10f,15f); lineTo(14f,15f); lineTo(14f,21f); lineTo(19f,21f); lineTo(19f,9f) } }
                Symbol.Calendar -> {
                    drawRoundRect(color, Offset(3f,5f), Size(18f,16f), CornerRadius(3f), style=stroke)
                    line(3f,10f,21f,10f); line(8f,3f,8f,7f); line(16f,3f,16f,7f)
                    listOf(8f,12f,16f).forEach { x -> drawCircle(color,1f,Offset(x,14f)) }
                    listOf(8f,12f).forEach { x -> drawCircle(color,1f,Offset(x,18f)) }
                }
                Symbol.Chart -> { line(4f,20f,21f,20f); drawRoundRect(color,Offset(5f,12f),Size(3f,8f),CornerRadius(1f)); drawRoundRect(color,Offset(11f,8f),Size(3f,12f),CornerRadius(1f)); drawRoundRect(color,Offset(17f,3f),Size(3f,17f),CornerRadius(1f)) }
                Symbol.People -> { drawCircle(color,3f,Offset(8f,7f),style=stroke); drawCircle(color,2.6f,Offset(17f,9f),style=stroke); drawArc(color,180f,180f,false,Offset(2f,13f),Size(12f,10f),style=stroke); drawArc(color,270f,180f,false,Offset(13f,15f),Size(8f,7f),style=stroke) }
                Symbol.Check -> path { moveTo(5f,12f); lineTo(10f,17f); lineTo(19f,7f) }
                Symbol.Plus -> { line(12f,5f,12f,19f); line(5f,12f,19f,12f) }
                Symbol.Back -> path { moveTo(15f,5f); lineTo(8f,12f); lineTo(15f,19f) }
                Symbol.Next -> path { moveTo(9f,5f); lineTo(16f,12f); lineTo(9f,19f) }
                Symbol.More -> listOf(5f,12f,19f).forEach { drawCircle(color,1.6f,Offset(it,12f)) }
                Symbol.Share -> { drawCircle(color,2.5f,Offset(6f,12f),style=stroke); drawCircle(color,2.5f,Offset(18f,5f),style=stroke); drawCircle(color,2.5f,Offset(18f,19f),style=stroke); line(8f,11f,16f,6f); line(8f,13f,16f,18f) }
                Symbol.Copy -> { drawRoundRect(color,Offset(8f,7f),Size(12f,14f),CornerRadius(2f),style=stroke); path { moveTo(15f,4f); lineTo(5f,4f); cubicTo(3f,4f,3f,4f,3f,6f); lineTo(3f,16f) } }
                Symbol.Book -> path { moveTo(12f,6f); cubicTo(9f,3f,5f,3f,3f,4f); lineTo(3f,19f); cubicTo(6f,18f,9f,18f,12f,21f); cubicTo(15f,18f,18f,18f,21f,19f); lineTo(21f,4f); cubicTo(18f,3f,15f,3f,12f,6f); lineTo(12f,21f) }
                Symbol.Drop -> path { moveTo(12f,3f); cubicTo(9f,8f,5f,12f,5f,15f); cubicTo(5f,24f,19f,24f,19f,15f); cubicTo(19f,12f,15f,8f,12f,3f); close() }
                Symbol.Leaf -> { path { moveTo(5f,19f); cubicTo(2f,8f,11f,3f,20f,4f); cubicTo(21f,13f,15f,21f,5f,19f); close() }; path { moveTo(3f,22f); cubicTo(6f,17f,10f,12f,16f,8f) } }
            }
        }
    }
}

@Composable
internal fun TogetherArt(modifier: Modifier = Modifier) {
    Canvas(modifier.size(144.dp).semantics { contentDescription = "서로 기대어 있는 두 개의 작은 하트" }) {
        val unit = size.minDimension / 160f
        withTransform({ scale(unit, unit, Offset.Zero) }) {
            drawCircle(Color(0xFFF5E3DA),61f,Offset(80f,83f))
            drawCircle(Color.White.copy(alpha=.7f),5f,Offset(136f,40f))
            drawCircle(Lilac.copy(alpha=.3f),3f,Offset(24f,30f))
            drawOval(Coral.copy(alpha=.08f),Offset(32f,132f),Size(98f,10f))
            rotate(-12f,Offset(62f,91f)) {
                drawRoundRect(Color(0xFFE5DCEF),Offset(28f,57f),Size(65f,72f),CornerRadius(25f))
                drawCircle(Ink,2f,Offset(50f,92f)); drawCircle(Ink,2f,Offset(69f,92f))
                drawArc(Ink,10f,160f,false,Offset(55f,96f),Size(10f,6f),style=Stroke(1.8f,cap=StrokeCap.Round))
                drawCircle(Coral.copy(alpha=.25f),4f,Offset(43f,99f))
            }
            rotate(10f,Offset(105f,92f)) {
                drawRoundRect(Color(0xFFECB7A7),Offset(79f,66f),Size(57f,65f),CornerRadius(23f))
                drawCircle(Ink,2f,Offset(96f,95f)); drawCircle(Ink,2f,Offset(113f,95f))
                drawArc(Ink,10f,160f,false,Offset(100f,99f),Size(9f,5f),style=Stroke(1.8f,cap=StrokeCap.Round))
                drawCircle(Coral.copy(alpha=.3f),3.7f,Offset(120f,102f))
            }
            drawPath(Path().apply { moveTo(82f,48f); cubicTo(68f,39f,69f,28f,77f,30f); cubicTo(82f,31f,83f,34f,83f,35f); cubicTo(89f,22f,102f,33f,93f,40f); close() }, Coral)
            drawLine(Sage,Offset(35f,57f),Offset(30f,45f),2f,StrokeCap.Round)
            drawOval(Sage.copy(alpha=.65f),Offset(23f,39f),Size(12f,7f))
            drawLine(Lilac,Offset(131f,62f),Offset(141f,62f),1.7f,StrokeCap.Round)
            drawLine(Lilac,Offset(136f,57f),Offset(136f,67f),1.7f,StrokeCap.Round)
        }
    }
}

@Composable
internal fun SoftCard(modifier: Modifier = Modifier, color: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), color=color, shape=RoundedCornerShape(24.dp),
        border=if(color==Color.White) androidx.compose.foundation.BorderStroke(1.dp,Line) else null) {
        Column(Modifier.padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp), content=content)
    }
}

@Composable
internal fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(title,color=Ink,fontSize=25.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.6).sp)
        subtitle?.let { Text(it,color=Muted,fontSize=13.sp,lineHeight=20.sp) }
    }
}

@Composable
internal fun Avatar(name: String, partner: Boolean = false, size: Int = 38) {
    Box(Modifier.size(size.dp).background(if(partner) Lavender else Peach,CircleShape),contentAlignment=Alignment.Center) {
        Text(name.take(1).ifEmpty { "?" },color=if(partner) Lilac else Coral,fontSize=(size*.38f).sp,fontWeight=FontWeight.Bold)
    }
}

@Composable
internal fun StatusPill(text: String, color: Color = Sage, background: Color = Mint) {
    Row(Modifier.background(background,RoundedCornerShape(50)).padding(horizontal=10.dp,vertical=5.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(4.dp).background(color,CircleShape))
        Text(text,color=color,fontSize=11.sp,fontWeight=FontWeight.Medium)
    }
}

@Composable
internal fun Segments(labels: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    Row(modifier.fillMaxWidth().background(Line.copy(alpha=.7f),RoundedCornerShape(16.dp)).padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed { i, label ->
            val bg by animateColorAsState(if(i==selected) Color.White else Color.Transparent,label="segment")
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(bg).clickable { onSelect(i) }.padding(horizontal=8.dp,vertical=13.dp),contentAlignment=Alignment.Center) {
                Text(label,color=if(i==selected) Ink else Muted,fontSize=13.sp,fontWeight=if(i==selected) FontWeight.SemiBold else FontWeight.Normal,maxLines=1)
            }
        }
    }
}

@Composable
internal fun PrimaryAction(text: String, enabled: Boolean = true, symbol: Symbol? = null, onClick: () -> Unit) {
    Button(onClick=onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(16.dp),
        colors=ButtonDefaults.buttonColors(containerColor=Coral,contentColor=Color.White),contentPadding=PaddingValues(14.dp)) {
        symbol?.let { Mark(it,color=Color.White,modifier=Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text,fontSize=14.sp,fontWeight=FontWeight.SemiBold)
    }
}

@Composable
internal fun OwnerSwitch(vm: DailyViewModel, selected: Int, onSelect: (Int) -> Unit): String {
    val ids = vm.names.keys.sortedBy { if(it==vm.uid) 0 else 1 }
    val labels = ids.map { if(it==vm.uid) "${vm.names[it]} · 나" else "${vm.names[it]} · 짝꿍" }
    if(labels.size>1) Segments(labels,selected.coerceAtMost(labels.lastIndex),onSelect=onSelect)
    else if(labels.size==1) Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Avatar(vm.names[ids.first()].orEmpty(),size=26)
        Text(labels.first(),color=Muted,fontSize=12.sp,fontWeight=FontWeight.Medium)
    }
    return ids.getOrNull(selected) ?: ids.firstOrNull() ?: vm.uid
}

internal fun goalSymbol(title: String): Symbol = when {
    title.contains("책") || title.contains("읽") || title.contains("공부") -> Symbol.Book
    title.contains("물") || title.contains("음료") -> Symbol.Drop
    title.contains("운동") || title.contains("걷") || title.contains("산책") -> Symbol.Leaf
    else -> Symbol.Heart
}
