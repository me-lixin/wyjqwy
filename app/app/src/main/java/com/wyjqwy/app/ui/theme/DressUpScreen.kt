package com.wyjqwy.app.ui.theme

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private data class DressThemeItem(
    val name: String,
    val primaryColor: Color,
    val textureType: ThemeBackgroundManager.TextureType
)

@Composable
fun DressUpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val options = remember {
        listOf(
            DressThemeItem("默认", Color(0xFFF4DA3A), ThemeBackgroundManager.TextureType.GRADIENT_FLOW),
            DressThemeItem("火焰橙", Color(0xFFFF744F), ThemeBackgroundManager.TextureType.GRADIENT_FLOW),
            DressThemeItem("琉璃绿", Color(0xFF21A97A), ThemeBackgroundManager.TextureType.PATTERN_OVERLAY),
            DressThemeItem("青莲紫", Color(0xFF8F5AF6), ThemeBackgroundManager.TextureType.GRADIENT_FLOW),
            DressThemeItem("樱绯红", Color(0xFFEF5A93), ThemeBackgroundManager.TextureType.GRADIENT_FLOW),
            DressThemeItem("晴空蓝", Color(0xFF3A7BFF), ThemeBackgroundManager.TextureType.PATTERN_OVERLAY),
            DressThemeItem("林间月", Color(0xFF9BB7C8), ThemeBackgroundManager.TextureType.NOISE_TEXTURE),
            DressThemeItem("黄昏沙丘", Color(0xFFD7A6B0), ThemeBackgroundManager.TextureType.NOISE_TEXTURE)
        )
    }
    val savedColor = remember { ThemeUtils.getPrimaryColor(context) }
    val savedType = remember { ThemeUtils.getTextureType(context) }
    val initialIndex = remember(savedColor, savedType) {
        options.indexOfFirst { it.primaryColor.toArgb() == savedColor && it.textureType == savedType }
            .takeIf { it >= 0 } ?: 0
    }
    var selected by remember(initialIndex) { mutableIntStateOf(initialIndex) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(options[selected].primaryColor)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = BookColors.TextBlack,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() }
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(10.dp))
            Text(
                text = "个性装扮",
                color = BookColors.TextBlack,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(options.indices.toList()) { idx ->
                val item = options[idx]
                DressThemeCard(
                    item = item,
                    selected = idx == selected,
                    onClick = {
                        selected = idx
                        ThemeUtils.saveTheme(context, item.primaryColor.toArgb(), item.textureType)
                    }
                )
            }
        }
    }
}

@Composable
private fun DressThemeCard(
    item: DressThemeItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF2F2F2))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box {
            TextureBackgroundContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .clip(RoundedCornerShape(12.dp)),
                type = item.textureType,
                primaryColor = item.primaryColor
            ) {}
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF2CB96A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "已选中",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        Text(
            text = item.name,
            color = BookColors.TextBlack,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun TextureBackgroundContainer(
    modifier: Modifier = Modifier,
    type: ThemeBackgroundManager.TextureType,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    val manager = remember { ThemeBackgroundManager() }
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                FrameLayout(context).also { view ->
                    manager.applyTextureToView(view, type, primaryColor.toArgb())
                }
            },
            update = { view ->
                manager.applyTextureToView(view, type, primaryColor.toArgb())
            }
        )
        content()
    }
}
