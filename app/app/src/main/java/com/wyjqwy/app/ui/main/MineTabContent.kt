package com.wyjqwy.app.ui.main

import android.graphics.BitmapFactory
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.layout.onSizeChanged
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.DecoratedThemeIcon
import com.wyjqwy.app.ui.theme.ThemeBackgroundManager
import com.wyjqwy.app.ui.theme.ThemeUtils
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.time.LocalDate
import kotlin.math.abs

@Composable
fun MineTabScreen(
    state: AppUiState,
    vm: AppViewModel,
    onOpenLoginRegister: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenDressUp: () -> Unit,
    onOpenAccountSettings: () -> Unit,
    profileRefreshTick: Int
) {
    val primaryColor = rememberThemePrimaryColor()
    val mineTextScale = rememberMineTextScale()
    val context = LocalContext.current
    val selectedTexture = remember { ThemeUtils.getTextureType(context) }
    val loginPhone = state.loginPhone
    var localProfile by remember(loginPhone) { mutableStateOf(loadProfileByPhone(context, loginPhone)) }
    val headerAvatarBitmap = remember(localProfile.avatarPath) {
        if (localProfile.avatarPath.isBlank()) null else BitmapFactory.decodeFile(localProfile.avatarPath)
    }
    LaunchedEffect(loginPhone, profileRefreshTick) {
        localProfile = loadProfileByPhone(context, loginPhone)
    }
    // 进入“我的”页后拉取一次全量总览统计，避免落回当月明细导致总天数/总笔数偏小。
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) vm.ensureOverviewStatsLoaded()
    }
    // 本地轻量统计仅作为全量统计尚未返回时的兜底展示。
    val investCount = remember(state.transactions) {
        state.transactions.count { it.type == 1 && it.categoryName.contains("投资", ignoreCase = true) }
    }
    val totalDays = remember(state.transactions) {
        state.transactions.mapNotNull { it.parsedOccurredAt?.toLocalDate() }.distinct().size
    }
    val totalCount = state.transactions.size
    // 取消全量历史统计预加载（2000年至今），保持懒加载策略。
    val overviewDaysText = when {
        state.overviewTotalDays != null -> state.overviewTotalDays.toString()
        else -> totalDays.toString()
    }
    val overviewCountText = when {
        state.overviewTotalCount != null -> state.overviewTotalCount.toString()
        else -> totalCount.toString()
    }

    Column(Modifier.fillMaxSize()) {
        TextureBackgroundContainer(
            modifier = Modifier.fillMaxWidth(),
            type = selectedTexture,
            primaryColor = primaryColor
        ) {
            Column(
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BookColors.White)
                                .clickable {
                                    if (state.loggedIn) onOpenAccountSettings() else onOpenLoginRegister()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (headerAvatarBitmap != null) {
                                Image(
                                    bitmap = headerAvatarBitmap.asImageBitmap(),
                                    contentDescription = "账号设置",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "账号设置",
                                    tint = BookColors.TextGray,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = if (state.loggedIn) {
                                localProfile.nickname.ifBlank { "未设置昵称" }
                            } else {
                                "点击登录"
                            },
                            color = BookColors.TextBlack,
                            style = MaterialTheme.typography.displaySmall.scaled(mineTextScale),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MineStatCell(value = investCount.toString(), label = "定投次数", textScale = mineTextScale, modifier = Modifier.weight(1f))
                    MineStatCell(value = overviewDaysText, label = "记账总天数", textScale = mineTextScale, modifier = Modifier.weight(1f))
                    MineStatCell(value = overviewCountText, label = "记账总笔数", textScale = mineTextScale, modifier = Modifier.weight(1f))
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BookColors.Background)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MineMenuCard {
                    MineMenuRow(icon = Icons.Outlined.SwapHoriz, title = "导入数据", onClick = onOpenImport)
                    HorizontalDivider(color = BookColors.Line)
                    MineMenuRow(icon = Icons.Outlined.WorkOutline, title = "导出数据", onClick = onOpenExport)
                    HorizontalDivider(color = BookColors.Line)
                    MineMenuRow(icon = Icons.Outlined.Face, title = "个性装扮", onClick = onOpenDressUp)
                    HorizontalDivider(color = BookColors.Line)
                    MineMenuRow(
                        icon = Icons.Outlined.Person,
                        title = if (state.loggedIn) "退出登录" else "未登录（登录 / 注册）",
                        titleColor = BookColors.RedExpense,
                        onClick = {
                            if (state.loggedIn) vm.logout() else onOpenLoginRegister()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MineFeaturePlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val mineTextScale = rememberMineTextScale()
    val context = LocalContext.current
    val selectedTexture = remember { ThemeUtils.getTextureType(context) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TextureBackgroundContainer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            type = selectedTexture,
            primaryColor = primaryColor
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = BookColors.TextBlack,
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = BookColors.TextBlack,
                    style = MaterialTheme.typography.titleLarge.scaled(mineTextScale),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("${title}功能开发中", color = BookColors.TextGray, fontSize = 15.sp)
        }
    }
}

@Composable
private fun MineStatCell(value: String, label: String, textScale: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = BookColors.TextBlack,
            style = MaterialTheme.typography.displaySmall.scaled(textScale),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = BookColors.TextBlack.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyLarge.scaled(textScale)
        )
    }
}

@Composable
private fun MineMenuCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BookColors.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        content()
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
    var measuredHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val measuredHeightDp = with(density) { measuredHeightPx.toDp() }
    Box(
        modifier = modifier.onSizeChanged {
            measuredHeightPx = it.height
        }
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(measuredHeightDp),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    manager.applyTextureToView(this, type, primaryColor.toArgb())
                }
            },
            update = { view ->
                manager.applyTextureToView(view, type, primaryColor.toArgb())
            }
        )
        content()
    }
}

@Composable
private fun MineMenuRow(
    icon: ImageVector,
    title: String,
    titleColor: Color = BookColors.TextBlack,
    onClick: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val mineTextScale = rememberMineTextScale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            DecoratedThemeIcon(
                icon = icon,
                primaryColor = primaryColor,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            color = titleColor,
            style = MaterialTheme.typography.titleLarge.scaled(mineTextScale),
            modifier = Modifier.weight(1f)
        )
        Text(
            ">",
            color = BookColors.TextGray,
            style = MaterialTheme.typography.titleLarge.scaled(mineTextScale)
        )
    }
}

@Composable
private fun rememberMineTextScale(): Float {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp <= 360 -> 0.92f
        widthDp >= 420 -> 1.08f
        else -> 1f
    }
}

private fun TextStyle.scaled(scale: Float): TextStyle {
    return copy(fontSize = fontSize.scaled(scale), lineHeight = lineHeight.scaled(scale))
}

private fun TextUnit.scaled(scale: Float): TextUnit {
    return if (value.isNaN()) this else (value * scale).sp
}
