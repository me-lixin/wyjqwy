package com.wyjqwy.app.ui.category

import androidx.compose.ui.graphics.vector.ImageVector
import com.wyjqwy.app.data.Category

/**
 * 分类选择格一项：系统预设 + 用户自建（来自接口）合并后的展示模型。
 */
data class CategoryPickerEntry(
    val name: String,
    val iconKey: String,
    val icon: ImageVector,
    /** 接口分类 id；仅本地预设且接口暂无行时为 0 */
    val serverId: Long,
    val isUserCustom: Boolean
)

fun mergedPickerEntries(
    txType: Int,
    presets: List<CategoryPreset>,
    apiCategories: List<Category>,
    savedOrder: List<String>
): List<CategoryPickerEntry> {
    val typeRows = apiCategories.filter { it.type == txType }
    val apiByName = typeRows.associateBy { it.name }
    val presetNames = presets.map { it.name }.toSet()

    val fromPresets = presets.map { p ->
        val api = apiByName[p.name]
        val key = api?.icon?.takeIf { !it.isNullOrBlank() } ?: p.iconKey
        CategoryPickerEntry(
            name = p.name,
            iconKey = key,
            icon = categoryIconForIconKey(key),
            serverId = api?.id ?: 0L,
            // 系统默认分类固定，不作为“可编辑/可删除”的自定义项
            isUserCustom = false
        )
    }

    val customs = typeRows
        .filter { it.isUserCategory && it.name !in presetNames }
        .map { c ->
            val key = c.icon?.takeIf { it.isNotBlank() } ?: "category"
            CategoryPickerEntry(
                name = c.name,
                iconKey = key,
                icon = categoryIconForIconKey(key),
                serverId = c.id,
                isUserCustom = true
            )
        }

    val combined = fromPresets + customs
    return reorderEntriesByName(combined, savedOrder)
}

fun reorderEntriesByName(
    entries: List<CategoryPickerEntry>,
    savedNames: List<String>
): List<CategoryPickerEntry> {
    if (savedNames.isEmpty()) return entries
    val byName = entries.associateBy { it.name }
    val ordered = savedNames.mapNotNull { byName[it] }.toMutableList()
    val used = ordered.map { it.name }.toHashSet()
    entries.filter { it.name !in used }.forEach { ordered.add(it) }
    return ordered
}
