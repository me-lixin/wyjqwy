package com.wyjqwy.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    // AppBar title
    titleLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.W600
    ),
    // Page/module section title
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.W600
    ),
    // Core numeric data
    displaySmall = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.W700
    ),
    // Primary content text
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.W400
    ),
    // Secondary text / timestamp
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.W400,
        color = BookColors.TextGray
    ),
    // Tiny labels
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.W400
    )
)
