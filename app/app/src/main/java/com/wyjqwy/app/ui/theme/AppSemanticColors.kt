package com.wyjqwy.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 随浅色 / 深色主题变化的语义色（页面背景、卡片、正文与次要文字等）。
 * 品牌色（主题色）仍由 [rememberThemePrimaryColor] 单独管理。
 */
data class AppSemanticColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val categoryGridCircle: Color,
    val categoryGridIcon: Color,
    val tabBarDivider: Color,
    /** 收入金额等正向强调色 */
    val income: Color,
    /** 支出 / 错误强调（与 [BookColors.RedExpense] 同系） */
    val expense: Color
)

internal fun lightSemanticColors(): AppSemanticColors = AppSemanticColors(
    background = BookColors.Background,
    surface = BookColors.White,
    surfaceMuted = BookColors.Background,
    textPrimary = BookColors.TextBlack,
    textSecondary = BookColors.TextGray,
    textTertiary = BookColors.TextLight,
    divider = BookColors.Line,
    categoryGridCircle = BookColors.CategoryGridCircle,
    categoryGridIcon = BookColors.CategoryGridIcon,
    tabBarDivider = BookColors.TabBarTopLine,
    income = Color(0xFF2E7D32),
    expense = BookColors.RedExpense
)

internal fun darkSemanticColors(): AppSemanticColors = AppSemanticColors(
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceMuted = Color(0xFF2C2C2E),
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFFAEAEB2),
    textTertiary = Color(0xFF8E8E93),
    divider = Color(0xFF38383A),
    categoryGridCircle = Color(0xFF3A3A3C),
    categoryGridIcon = Color(0xFFC7C7CC),
    tabBarDivider = Color.White.copy(alpha = 0.12f),
    income = Color(0xFF81C784),
    expense = Color(0xFFFFAB91)
)

val LocalAppSemanticColors = compositionLocalOf { lightSemanticColors() }

@Composable
fun themeColors(): AppSemanticColors = LocalAppSemanticColors.current
