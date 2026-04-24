package com.wyjqwy.app.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

object ThemeUtils {
    private const val PREFS_NAME = "wyjqwy_theme_runtime"
    private const val KEY_PRIMARY_COLOR = "primary_color"
    private const val KEY_TEXTURE_TYPE = "texture_type"

    fun getPrimaryColor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_PRIMARY_COLOR, BookColors.BrandTeal.toArgb())
    }

    fun setPrimaryColor(context: Context, color: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PRIMARY_COLOR, color)
            .apply()
    }

    fun getTextureType(context: Context): ThemeBackgroundManager.TextureType {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TEXTURE_TYPE, ThemeBackgroundManager.TextureType.GRADIENT_FLOW.name)
            ?: ThemeBackgroundManager.TextureType.GRADIENT_FLOW.name
        return ThemeBackgroundManager.TextureType.entries.firstOrNull { it.name == raw }
            ?: ThemeBackgroundManager.TextureType.GRADIENT_FLOW
    }

    fun setTextureType(context: Context, type: ThemeBackgroundManager.TextureType) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TEXTURE_TYPE, type.name)
            .apply()
    }

    fun saveTheme(context: Context, color: Int, type: ThemeBackgroundManager.TextureType) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PRIMARY_COLOR, color)
            .putString(KEY_TEXTURE_TYPE, type.name)
            .apply()
    }
}

@Composable
fun rememberThemePrimaryColor(): Color {
    val context = LocalContext.current
    return remember { Color(ThemeUtils.getPrimaryColor(context)) }
}
