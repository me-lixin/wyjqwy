package com.wyjqwy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = BookColors.Main,
            onPrimary = BookColors.TextBlack,
            background = Color(0xFF1C1C1E),
            surface = Color(0xFF2C2C2E),
            onBackground = Color(0xFFE5E5E5),
            onSurface = Color(0xFFE5E5E5)
        )
    } else {
        lightColorScheme(
            primary = BookColors.Main,
            onPrimary = BookColors.TextBlack,
            secondary = BookColors.MainDark,
            onSecondary = BookColors.TextBlack,
            background = BookColors.Background,
            surface = BookColors.White,
            onBackground = BookColors.TextBlack,
            onSurface = BookColors.TextBlack,
            outline = BookColors.Line,
            error = BookColors.RedExpense
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
