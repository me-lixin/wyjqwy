package com.wyjqwy.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor

private data class BottomTab(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val isCenter: Boolean = false
)

@Composable
fun SharkBottomBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onCenterAdd: () -> Unit,
    onCenterAddPositionChanged: (Offset) -> Unit,
    onVoiceGestureStart: () -> Unit,
    onVoiceGestureMove: (Offset) -> Unit,
    onVoiceGestureEnd: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val currentOnVoiceStart by rememberUpdatedState(onVoiceGestureStart)
    val currentOnVoiceMove by rememberUpdatedState(onVoiceGestureMove)
    val currentOnVoiceEnd by rememberUpdatedState(onVoiceGestureEnd)
    val currentOnCenterAdd by rememberUpdatedState(onCenterAdd)

    val tabs = listOf(
        BottomTab(0, "明细", Icons.Outlined.Article),
        BottomTab(1, "图表", Icons.Outlined.BarChart),
        BottomTab(2, "记账", Icons.Outlined.Add, isCenter = true),
        BottomTab(3, "定投", Icons.Outlined.Explore),
        BottomTab(4, "我的", Icons.Outlined.Person)
    )
    Column(
        Modifier
            .fillMaxWidth()
            .background(BookColors.White)
    ) {
        HorizontalDivider(color = BookColors.TabBarTopLine, thickness = 1.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
        ) {
            tabs.forEach { tab ->
                if (tab.isCenter) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        var centerButtonCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
                        Box(
                            Modifier
                                .offset(y = (-20).dp)
                                .size(52.dp)
                                .onGloballyPositioned { c ->
                                    centerButtonCoords = c
                                    onCenterAddPositionChanged(c.boundsInWindow().center)
                                }
                                .clip(CircleShape)
                                .background(primaryColor)
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { currentOnVoiceStart() },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val coords = centerButtonCoords
                                            if (coords != null) {
                                                val windowPoint = coords.localToWindow(change.position)
                                                currentOnVoiceMove(windowPoint)
                                            }
                                        },
                                        onDragEnd = { currentOnVoiceEnd() },
                                        onDragCancel = { currentOnVoiceEnd() }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { currentOnCenterAdd() }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = BookColors.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            color = BookColors.TextGray,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 14.dp)
                        )
                    }
                } else {
                    val selected = selectedIndex == tab.index
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onSelect(tab.index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) primaryColor else BookColors.TextGray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            color = if (selected) primaryColor else BookColors.TextGray,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
