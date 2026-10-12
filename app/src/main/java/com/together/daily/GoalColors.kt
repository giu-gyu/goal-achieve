package com.together.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    GoalPalette("로즈",Color(0xFF9D507B),Color(0xFFFAEDF5))
)
internal fun goalColorIndex(goal:Goal)=goal.colorIndex?.takeIf {it in GoalPalettes.indices}?:Math.floorMod(goal.id.hashCode(),GoalPalettes.size)
internal fun goalPalette(goal:Goal)=GoalPalettes[goalColorIndex(goal)]

@Composable
internal fun GoalColorPicker(selected:Int,enabled:Boolean,onSelect:(Int)->Unit) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        Text("색상",fontSize=12.sp,color=Muted)
        Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            GoalPalettes.forEachIndexed {index,palette ->
                Box(Modifier.size(32.dp).selectable(selected==index,enabled=enabled,role=Role.RadioButton,onClick={onSelect(index)})
                    .semantics {contentDescription="${palette.name} 색상"}.padding(3.dp)
                    .border(if(selected==index)2.dp else 0.dp,if(selected==index)palette.accent else Color.Transparent,CircleShape)
                    .padding(4.dp).background(palette.accent,CircleShape))
            }
        }
    }
}
