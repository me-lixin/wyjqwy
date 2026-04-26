package com.wyjqwy.app.ui.category.reorder

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(
    context: Context
) : ViewModel() {
    private val prefs = context.getSharedPreferences("category_reorder_single_user", Context.MODE_PRIVATE)
    private val _categories = mutableStateListOf<Category>()
    val categories: List<Category> get() = _categories

    init {
        loadCategories()
    }

    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _categories.indices || toIndex !in _categories.indices || fromIndex == toIndex) return
        _categories.add(toIndex, _categories.removeAt(fromIndex))
    }

    fun persistOrder() {
        viewModelScope.launch {
            val ids = _categories.joinToString("|") { it.id.toString() }
            prefs.edit().putString("category_ids_order", ids).apply()
        }
    }

    private fun loadCategories() {
        val default = defaultCategories()
        val saved = prefs.getString("category_ids_order", null).orEmpty()
        if (saved.isBlank()) {
            _categories.clear()
            _categories.addAll(default)
            return
        }
        val idMap = default.associateBy { it.id }
        val ordered = saved.split("|")
            .mapNotNull { it.toLongOrNull() }
            .mapNotNull { idMap[it] }
            .toMutableList()
        val used = ordered.map { it.id }.toHashSet()
        default.filterNot { it.id in used }.forEach { ordered.add(it) }
        _categories.clear()
        _categories.addAll(ordered)
    }

    private fun defaultCategories(): List<Category> = listOf(
        Category(1L, "餐饮", android.R.drawable.ic_menu_crop),
        Category(2L, "购物", android.R.drawable.ic_menu_gallery),
        Category(3L, "交通", android.R.drawable.ic_menu_directions),
        Category(4L, "住房", android.R.drawable.ic_menu_myplaces),
        Category(5L, "娱乐", android.R.drawable.ic_menu_slideshow),
        Category(6L, "医疗", android.R.drawable.ic_menu_info_details),
        Category(7L, "教育", android.R.drawable.ic_menu_edit),
        Category(8L, "其他", android.R.drawable.ic_menu_help)
    )
}
