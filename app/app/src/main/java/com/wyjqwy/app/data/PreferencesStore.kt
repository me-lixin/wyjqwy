package com.wyjqwy.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "wyjqwy_prefs")

class PreferencesStore(private val context: Context) {
    private val sp = context.getSharedPreferences("wyjqwy_local_prefs", Context.MODE_PRIVATE)

    private object Keys {
        val THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
        val EXPENSE_CATEGORY_ORDER: Preferences.Key<String> = stringPreferencesKey("expense_category_order")
        val INCOME_CATEGORY_ORDER: Preferences.Key<String> = stringPreferencesKey("income_category_order")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    fun categoryOrder(type: Int): Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = when (type) {
            1 -> prefs[Keys.EXPENSE_CATEGORY_ORDER]
            2 -> prefs[Keys.INCOME_CATEGORY_ORDER]
            else -> null
        }.orEmpty()
        if (raw.isBlank()) emptyList() else raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    suspend fun setCategoryOrder(type: Int, names: List<String>) {
        val value = names.joinToString("|")
        context.dataStore.edit { prefs ->
            when (type) {
                1 -> prefs[Keys.EXPENSE_CATEGORY_ORDER] = value
                2 -> prefs[Keys.INCOME_CATEGORY_ORDER] = value
            }
        }
    }

    fun getCategoryOrderNow(type: Int): List<String> {
        val raw = when (type) {
            1 -> sp.getString("expense_category_order_now", "")
            2 -> sp.getString("income_category_order_now", "")
            else -> ""
        }.orEmpty()
        return if (raw.isBlank()) emptyList() else raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun setCategoryOrderNow(type: Int, names: List<String>) {
        val value = names.joinToString("|")
        when (type) {
            1 -> sp.edit().putString("expense_category_order_now", value).apply()
            2 -> sp.edit().putString("income_category_order_now", value).apply()
        }
    }

    fun renameInCategoryOrderNow(type: Int, oldName: String, newName: String) {
        if (oldName == newName) return
        val list = getCategoryOrderNow(type).map { if (it == oldName) newName else it }
        setCategoryOrderNow(type, list)
    }
}

