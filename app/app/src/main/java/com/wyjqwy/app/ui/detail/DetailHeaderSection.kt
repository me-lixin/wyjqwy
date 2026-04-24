package com.wyjqwy.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TemplateItem
import kotlin.math.roundToInt
import com.wyjqwy.app.ui.category.categoryIconForIconKey
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import com.wyjqwy.app.ui.util.toAmountText
import java.time.YearMonth

@Composable
internal fun DetailHeaderSection(
    appTitle: String,
    appLogo: String,
    selectedYearMonth: YearMonth,
    amountVisible: Boolean,
    totalIncome: Double,
    totalExpense: Double,
    templates: List<TemplateItem>,
    onToggleAmountVisible: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenYearMonthPicker: () -> Unit,
    onApplyTemplate: (TemplateItem) -> Unit,
    onDeleteTemplate: (TemplateItem) -> Unit,
    onTemplateDragActiveChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = rememberThemePrimaryColor()
    Box(
        modifier
            .fillMaxWidth()
            // 🌟 核心修改：使用 Brush.verticalGradient 实现从主题色到白色的垂直渐变
            .background(
                brush = Brush.verticalGradient(
                    // 0.0f 是顶部，1.0f 是底部
                    0.0f to primaryColor,   // 从顶部开始是主题色
                    0.8f to primaryColor,   // 保持主题色一直到 60% 的高度（这就相当于把“渐变的开始”往下推了）
                    1.0f to Color.White     // 从 60% 到 100% 的区域发生渐变，最终变成白色
                )
            )
    ) {
        Column(Modifier.statusBarsPadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onToggleAmountVisible) {
                    Icon(
                        imageVector = if (amountVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "显示或隐藏金额",
                        tint = BookColors.TextBlack
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BookColors.White.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appLogo,
                            color = BookColors.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = appTitle,
                        color = BookColors.TextBlack,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onOpenCalendar) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = "日历", tint = BookColors.TextBlack)
                }
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "搜索", tint = BookColors.TextBlack)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .weight(0.6f)
                        .clickable { onOpenYearMonthPicker() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${selectedYearMonth.year}年", color = BookColors.TextBlack, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                String.format("%02d月", selectedYearMonth.monthValue),
                                color = BookColors.TextBlack,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light
                            )
                            Icon(
                                Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = BookColors.TextBlack,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .height(30.dp) // 控制分割线高度
                                    .width(1.dp)   // 线宽
                                    .background(BookColors.TextBlack.copy(alpha = 0.2f)) // 颜色和透明度
                            )

                        }
                    }
                }

                SummaryStat("收入", totalIncome, amountVisible, BookColors.TextBlack, Modifier.weight(1f))
                SummaryStat("支出", totalExpense, amountVisible, BookColors.TextBlack, Modifier.weight(1f))
            }
            TemplateQuickBar(
                templates = templates,
                onApplyTemplate = onApplyTemplate,
                onDeleteTemplate = onDeleteTemplate,
                onDragActiveChanged = onTemplateDragActiveChanged
            )
        }
    }
}

@Composable
private fun TemplateQuickBar(
    templates: List<TemplateItem>,
    onApplyTemplate: (TemplateItem) -> Unit,
    onDeleteTemplate: (TemplateItem) -> Unit,
    onDragActiveChanged: (Boolean) -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    var templateAreaInWindow: Rect? by remember { mutableStateOf(null) }
    val rowHeight = 74.dp
    // 用 background(shape) 画圆角底，不用 clip，否则拖出时会被截断；拖拽项 zIndex 提高后叠在兄弟模板之上
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp), clip = false)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp)
            )
            .onGloballyPositioned { templateAreaInWindow = it.boundsInWindow() }
            .padding(horizontal = 7.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
            contentAlignment = Alignment.Center
        ) {
            if (templates.isEmpty()) {
                Text(
                    text = "左滑明细可添加模板，长按可拖到区域外删除",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(templates.take(12), key = { it.id }) { item ->
                        DraggableTemplateItem(
                            item = item,
                            templateAreaInWindow = templateAreaInWindow,
                            primaryColor = primaryColor,
                            onApply = { onApplyTemplate(item) },
                            onDelete = { onDeleteTemplate(item) },
                            onDragActiveChanged = onDragActiveChanged
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableTemplateItem(
    item: TemplateItem,
    templateAreaInWindow: Rect?,
    primaryColor: androidx.compose.ui.graphics.Color,
    onApply: () -> Unit,
    onDelete: () -> Unit,
    onDragActiveChanged: (Boolean) -> Unit
) {
    var drag by remember(item.id) { mutableStateOf(Offset.Zero) }
    val itemCoordsRef = remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    val areaState = rememberUpdatedState(newValue = templateAreaInWindow)
    val icon = categoryIconForIconKey(item.categoryIcon).takeIf { !item.categoryIcon.isNullOrBlank() }
        ?: categoryIconForName(item.categoryName)
    val label = item.note?.takeIf { it.isNotBlank() } ?: item.categoryName
    val amountText = if (item.type == 2) "+${(item.amount ?: 0.0).toAmountText()}"
    else "-${(item.amount ?: 0.0).toAmountText()}"
    val amountColor = if (item.type == 2) androidx.compose.ui.graphics.Color(0xFF2E7D32) else BookColors.RedExpense

    val dragging = drag != Offset.Zero
    Box(
        modifier = Modifier
            .zIndex(if (dragging) 10_000f else 0f)
            .onGloballyPositioned { itemCoordsRef.value = it }
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .pointerInput(item.id) {
                var acc = Offset.Zero
                var startRect: Rect? = null
                detectDragGesturesAfterLongPress(
                    onDrag = { change, amount ->
                        if (startRect == null) {
                            startRect = itemCoordsRef.value?.boundsInWindow()
                            onDragActiveChanged(true)
                        }
                        change.consume()
                        acc += amount
                        drag = acc
                    },
                    onDragEnd = {
                        val r = startRect
                        val ar = areaState.value
                        if (r != null && ar != null) {
                            val endCenter = r.center + acc
                            if (!ar.contains(endCenter)) onDelete()
                        }
                        acc = Offset.Zero
                        startRect = null
                        drag = Offset.Zero
                        onDragActiveChanged(false)
                    },
                    onDragCancel = {
                        acc = Offset.Zero
                        startRect = null
                        drag = Offset.Zero
                        onDragActiveChanged(false)
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .clickable { onApply() }
                .padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TemplateItemVisual(
                icon = icon,
                label = label,
                amountText = amountText,
                amountColor = amountColor,
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
private fun TemplateItemVisual(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    amountText: String,
    amountColor: Color,
    primaryColor: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(primaryColor.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = primaryColor,
            modifier = Modifier.size(22.dp)
        )
    }
    Text(
        text = amountText,
        color = amountColor,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium
    )
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun SummaryStat(
    label: String,
    value: Double,
    visible: Boolean,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = BookColors.TextBlack, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (visible) value.toAmountText() else "****",
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}