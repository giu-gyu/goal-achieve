package com.together.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.selection.selectable

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class GoalPalette(val name:String,val accent:Color,val background:Color)
internal val GoalPalettes=listOf(
    GoalPalette("코랄",Color(0xFFAF4E5C),Color(0xFFFCEEF0)),
    GoalPalette("블루",Color(0xFF32668F),Color(0xFFEDF5FC)),
    GoalPalette("라벤더",Color(0xFF775493),Color(0xFFF4EFFA)),
    GoalPalette("민트",Color(0xFF377663),Color(0xFFEDF8F3)),
    GoalPalette("앰버",Color(0xFF8F681B),Color(0xFFFCF6E8)),
    GoalPalette("로즈",Color(0xFF9D507B),Color(0xFFFAEDF5)),
    GoalPalette("레드",Color(0xFFB63838),Color(0xFFFFEEEE)),
    GoalPalette("오렌지",Color(0xFFB96120),Color(0xFFFFF1E5)),
    GoalPalette("피치",Color(0xFFC47A57),Color(0xFFFFF3EC)),
    GoalPalette("옐로",Color(0xFF9A810C),Color(0xFFFFFBE0)),
    GoalPalette("라임",Color(0xFF748426),Color(0xFFF4F8E3)),
    GoalPalette("그린",Color(0xFF398345),Color(0xFFEDF8EF)),
    GoalPalette("포레스트",Color(0xFF28634D),Color(0xFFE9F4EF)),
    GoalPalette("틸",Color(0xFF237B7C),Color(0xFFE8F7F6)),
    GoalPalette("시안",Color(0xFF247F9D),Color(0xFFE9F7FC)),
    GoalPalette("네이비",Color(0xFF354D83),Color(0xFFEDF1FA)),
    GoalPalette("인디고",Color(0xFF56539D),Color(0xFFF0EFFB)),
    GoalPalette("퍼플",Color(0xFF8B45A0),Color(0xFFF8EEFC)),
    GoalPalette("브라운",Color(0xFF8B6049),Color(0xFFF6EFE9)),
    GoalPalette("슬레이트",Color(0xFF5A697C),Color(0xFFF0F3F6))
)
internal fun goalColorIndex(goal:Goal)=goal.colorIndex?.takeIf {it in GoalPalettes.indices}?:Math.floorMod(goal.id.hashCode(),GoalPalettes.size)
internal fun goalPalette(goal:Goal)=GoalPalettes[goalColorIndex(goal)]

@Composable
internal fun GoalColorPicker(selected:Int,enabled:Boolean,onSelect:(Int)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(2.dp)) {
        Text("색상 · ${GoalPalettes.getOrElse(selected) {GoalPalettes.first()}.name}",fontSize=11.sp,color=Muted)
        Row(Modifier.fillMaxWidth()) {
            GoalPalettes.forEachIndexed {index,palette ->
                Box(Modifier.weight(1f).aspectRatio(1f).background(palette.accent)
                    .selectable(selected==index,enabled=enabled,role=Role.RadioButton,onClick={onSelect(index)})
                    .semantics {contentDescription="${palette.name} 색상"}
                    .border(if(selected==index)2.dp else 0.dp,if(selected==index)Ink else Color.Transparent))
            }
        }
    }
}