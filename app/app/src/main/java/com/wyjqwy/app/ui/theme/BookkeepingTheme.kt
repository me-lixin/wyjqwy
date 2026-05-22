package com.wyjqwy.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.wyjqwy.app.data.ThemeMode

@Composable
fun BookkeepingTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val appColors = if (darkTheme) darkSemanticColors() else lightSemanticColors()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = BookColors.Main,
            onPrimary = BookColors.TextBlack,
            secondary = BookColors.MainDark,
            onSecondary = BookColors.TextBlack,
            background = appColors.background,
            surface = appColors.surface,
            surfaceVariant = appColors.surfaceMuted,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.divider,
            outlineVariant = appColors.divider,
            error = BookColors.RedExpense,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = BookColors.Main,
            onPrimary = BookColors.TextBlack,
            secondary = BookColors.MainDark,
            onSecondary = BookColors.TextBlack,
            background = appColors.background,
            surface = appColors.surface,
            surfaceVariant = appColors.surfaceMuted,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.divider,
            outlineVariant = appColors.divider,
            error = BookColors.RedExpense,
            onError = Color.White
        )
    }
    CompositionLocalProvider(LocalAppSemanticColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
