package com.together.daily

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun Fireworks(trigger:Int) {
    val progress=remember {Animatable(1f)}
    LaunchedEffect(trigger) {
        if(trigger>0) {
            progress.snapTo(0f)
            progress.animateTo(1f,tween(1800))
        }
    }
    if(progress.value<1f) Canvas(Modifier.fillMaxSize()) {
        val t=progress.value
        val colors=listOf(Coral,Sage,Lilac,Color(0xFFFFC857),Color(0xFF56B4E9))
        repeat(3) {burst ->
            val local=((t-burst*.12f)/.76f).coerceIn(0f,1f)
            if(local>0f&&local<1f) repeat(48) {i ->
                val angle=i*2*Math.PI/48
                val radius=size.minDimension*(.22f+(i%5)*.045f)*local
                val origin=Offset(size.width*(.2f+burst*.3f),size.height*(.3f+(burst%2)*.12f))
                val point=origin+Offset(cos(angle).toFloat()*radius,sin(angle).toFloat()*radius+local*local*110.dp.toPx())
                val tail=Offset(cos(angle).toFloat()*7.dp.toPx(),sin(angle).toFloat()*7.dp.toPx())
                drawLine(colors[(i+burst)%colors.size].copy(alpha=1f-local),point-tail,point,3.dp.toPx(),StrokeCap.Round)
            }
        }
    }
}
