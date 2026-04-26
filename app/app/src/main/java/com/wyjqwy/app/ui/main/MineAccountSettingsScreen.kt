package com.wyjqwy.app.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.SubPageTopBar
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.io.File
import java.io.FileOutputStream

data class LocalAccountProfile(
    val phone: String,
    val nickname: String = "",
    val gender: String = "",
    val avatarPath: String = ""
)

private object LocalAccountProfileStore {
    private const val PREFS_NAME = "wyjqwy_account_profile"

    fun load(context: Context, phone: String): LocalAccountProfile {
        if (phone.isBlank()) return LocalAccountProfile(phone = "")
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return LocalAccountProfile(
            phone = phone,
            nickname = sp.getString("nickname_$phone", "").orEmpty(),
            gender = sp.getString("gender_$phone", "").orEmpty(),
            avatarPath = sp.getString("avatar_$phone", "").orEmpty()
        )
    }

    fun save(context: Context, profile: LocalAccountProfile) {
        if (profile.phone.isBlank()) return
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putString("nickname_${profile.phone}", profile.nickname)
            .putString("gender_${profile.phone}", profile.gender)
            .putString("avatar_${profile.phone}", profile.avatarPath)
            .apply()
    }
}

fun loadProfileByPhone(context: Context, phone: String): LocalAccountProfile {
    return LocalAccountProfileStore.load(context, phone)
}

@Composable
fun MineAccountSettingsScreen(
    loginPhone: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = rememberThemePrimaryColor()
    var profile by remember(loginPhone) { mutableStateOf(loadProfileByPhone(context, loginPhone)) }
    var showAvatarAction by remember { mutableStateOf(false) }
    var showAvatarCrop by remember { mutableStateOf(false) }
    var pendingAvatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showNicknameEditor by remember { mutableStateOf(false) }
    var showGenderPicker by remember { mutableStateOf(false) }
    var nicknameDraft by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        LocalAccountProfileStore.save(context, profile)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val bitmap = uri?.let { decodeBitmapFromUri(context, it.toString()) }
        if (bitmap != null) {
            pendingAvatarBitmap = bitmap
            showAvatarCrop = true
        }
    }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            pendingAvatarBitmap = bitmap
            showAvatarCrop = true
        }
    }

    if (showAvatarAction) {
        AlertDialog(
            onDismissRequest = { showAvatarAction = false },
            title = { Text("选择头像") },
            text = { Text("请选择头像来源") },
            confirmButton = {
                TextButton(onClick = {
                    showAvatarAction = false
                    pickImageLauncher.launch("image/*")
                }) { Text("相册上传") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAvatarAction = false
                    takePictureLauncher.launch(null)
                }) { Text("拍照") }
            }
        )
    }

    if (showNicknameEditor) {
        AlertDialog(
            onDismissRequest = { showNicknameEditor = false },
            title = { Text("设置昵称") },
            text = {
                TextField(
                    value = nicknameDraft,
                    onValueChange = { nicknameDraft = it.take(16) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    profile = profile.copy(nickname = nicknameDraft.trim())
                    showNicknameEditor = false
                }) { Text("确定", color = primaryColor) }
            },
            dismissButton = {
                TextButton(onClick = { showNicknameEditor = false }) { Text("取消") }
            }
        )
    }

    if (showAvatarCrop && pendingAvatarBitmap != null) {
        AvatarCropDialog(
            source = pendingAvatarBitmap!!,
            onCancel = {
                showAvatarCrop = false
                pendingAvatarBitmap = null
            },
            onConfirm = { cropped ->
                val path = saveAvatarBitmap(context, loginPhone, cropped)
                if (path.isNotBlank()) {
                    profile = profile.copy(avatarPath = path)
                }
                showAvatarCrop = false
                pendingAvatarBitmap = null
            }
        )
    }

    if (showGenderPicker) {
        AlertDialog(
            onDismissRequest = { showGenderPicker = false },
            title = { Text("选择性别") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            profile = profile.copy(gender = "男")
                            showGenderPicker = false
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = profile.gender == "男", onClick = null)
                        Text("男")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            profile = profile.copy(gender = "女")
                            showGenderPicker = false
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = profile.gender == "女", onClick = null)
                        Text("女")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGenderPicker = false }) { Text("取消") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookColors.Background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            SubPageTopBar(title = "账号设置", onBack = onBack)
        }
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookColors.White)
        ) {
            AccountSettingRow(
                title = "头像",
                value = "",
                onClick = { showAvatarAction = true },
                valueContent = {
                    val avatarBitmap = remember(profile.avatarPath) { decodeAvatar(profile.avatarPath) }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BookColors.Line),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap.asImageBitmap(),
                                contentDescription = "头像",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "默认头像",
                                tint = BookColors.TextGray,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            )
            HorizontalDivider(color = BookColors.Line)
            AccountSettingRow(
                title = "手机号",
                value = if (loginPhone.isBlank()) "未登录" else loginPhone,
                enabled = false
            )
            HorizontalDivider(color = BookColors.Line)
            AccountSettingRow(
                title = "昵称",
                value = profile.nickname.ifBlank { "未设置" },
                onClick = {
                    nicknameDraft = profile.nickname
                    showNicknameEditor = true
                }
            )
            HorizontalDivider(color = BookColors.Line)
            AccountSettingRow(
                title = "性别",
                value = profile.gender.ifBlank { "未填写" },
                onClick = { showGenderPicker = true }
            )
        }
    }
}

@Composable
private fun AccountSettingRow(
    title: String,
    value: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    valueContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = BookColors.TextBlack,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.weight(1f))
        if (valueContent != null) {
            valueContent()
        } else {
            Text(
                text = value,
                color = BookColors.TextGray,
                style = MaterialTheme.typography.titleMedium
            )
        }
        if (enabled && onClick != null) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = BookColors.TextGray,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun decodeAvatar(path: String): Bitmap? {
    if (path.isBlank()) return null
    return runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
}

private fun decodeBitmapFromUri(context: Context, uriString: String): Bitmap? {
    val uri = android.net.Uri.parse(uriString)
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    }.getOrNull()
}

private fun saveAvatarBitmap(context: Context, phone: String, bitmap: Bitmap): String {
    if (phone.isBlank()) return ""
    return runCatching {
        val dir = File(context.filesDir, "account_avatars").apply { mkdirs() }
        val target = File(dir, "${phone}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        target.absolutePath
    }.getOrDefault("")
}

private fun centerCropSquare(src: Bitmap): Bitmap {
    val edge = minOf(src.width, src.height)
    val left = (src.width - edge) / 2
    val top = (src.height - edge) / 2
    val out = Bitmap.createBitmap(edge, edge, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(src, Rect(left, top, left + edge, top + edge), Rect(0, 0, edge, edge), paint)
    return out
}

@Composable
private fun AvatarCropDialog(
    source: Bitmap,
    onCancel: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    var scale by remember(source) { mutableStateOf(1f) }
    var offset by remember(source) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("裁剪头像") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(BookColors.Line)
                        .onSizeChanged { viewportSize = it }
                        .pointerInput(source, scale) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offset += Offset(dragAmount.x, dragAmount.y)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = source.asImageBitmap(),
                        contentDescription = "裁剪预览",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    )
                }
                Text("拖动调整位置，滑动缩放", color = BookColors.TextGray)
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 1f..3f
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val out = cropBitmapWithTransform(source, viewportSize, scale, offset)
                onConfirm(out)
            }) { Text("完成") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("取消") }
        }
    )
}

private fun cropBitmapWithTransform(
    src: Bitmap,
    viewport: IntSize,
    scale: Float,
    offset: Offset
): Bitmap {
    if (viewport.width <= 0 || viewport.height <= 0) return centerCropSquare(src)
    val view = minOf(viewport.width, viewport.height).toFloat()
    val baseScale = maxOf(view / src.width, view / src.height)
    val totalScale = baseScale * scale
    val drawnW = src.width * totalScale
    val drawnH = src.height * totalScale
    val left = (view - drawnW) / 2f + offset.x
    val top = (view - drawnH) / 2f + offset.y

    val srcLeft = ((0f - left) / totalScale).coerceIn(0f, src.width.toFloat())
    val srcTop = ((0f - top) / totalScale).coerceIn(0f, src.height.toFloat())
    val srcRight = ((view - left) / totalScale).coerceIn(0f, src.width.toFloat())
    val srcBottom = ((view - top) / totalScale).coerceIn(0f, src.height.toFloat())

    val rect = Rect(
        srcLeft.toInt(),
        srcTop.toInt(),
        srcRight.toInt().coerceAtLeast(srcLeft.toInt() + 1),
        srcBottom.toInt().coerceAtLeast(srcTop.toInt() + 1)
    )
    val out = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(src, rect, Rect(0, 0, 512, 512), paint)
    return out
}
