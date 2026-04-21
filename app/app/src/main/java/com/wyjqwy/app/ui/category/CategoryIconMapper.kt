package com.wyjqwy.app.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.ui.graphics.vector.ImageVector

/** 根据后端 icon 字段或本地 key 解析矢量图标（预设 + 管理页图库） */
fun categoryIconForIconKey(iconKey: String?): ImageVector {
    val key = iconKey?.trim().orEmpty()
    if (key.isEmpty()) return Icons.Outlined.Category
    expenseCategoryPresets.find { it.iconKey == key }?.let { return it.icon }
    incomeCategoryPresets.find { it.iconKey == key }?.let { return it.icon }
    for (section in expenseManageIconSections) {
        section.slots.find { it.iconKey == key }?.let { return it.icon }
    }
    for (section in incomeManageIconSections) {
        section.slots.find { it.iconKey == key }?.let { return it.icon }
    }
    return categoryIconForName(key.replace('_', ' '))
}
