@file:OptIn(ExperimentalMaterial3Api::class)

package com.wyjqwy.app.ui.main

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.wyjqwy.app.data.bill.BillCsv
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import com.wyjqwy.app.ui.theme.themeColors
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

enum class MineImportExportMode {
    Import,
    Export
}

private enum class ExportScope {
    ALL,
    BY_RANGE
}

@Composable
fun MineImportExportScreen(
    mode: MineImportExportMode,
    state: AppUiState,
    vm: AppViewModel,
    onBack: () -> Unit,
    onNeedLogin: () -> Unit
) {
    val tc = themeColors()
    val primaryColor = rememberThemePrimaryColor()
    val onPrimary = contentColorFor(primaryColor)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var busy by remember { mutableStateOf(false) }

    var exportScope by remember { mutableStateOf(ExportScope.ALL) }
    var rangeStart by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var rangeEnd by remember { mutableStateOf(LocalDate.now()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val pickCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            try {
                val text = context.contentResolver.openInputStream(uri)?.use { ins ->
                    ins.bufferedReader(Charsets.UTF_8).readText()
                }.orEmpty()
                if (text.isBlank()) {
                    Toast.makeText(context, "文件内容为空", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val result = vm.importBillCsvUtf8(text)
                result.fold(
                    onSuccess = { n ->
                        Toast.makeText(context, "成功导入 $n 条账单", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    onFailure = { e ->
                        val msg = e.message ?: "导入失败"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "导入失败", Toast.LENGTH_LONG).show()
            } finally {
                busy = false
            }
        }
    }

    if (showStartPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = rangeStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = 2000..LocalDate.now().year
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        rangeStart = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                    }
                    showStartPicker = false
                }) { Text("确定", color = primaryColor) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("取消", color = tc.textSecondary) }
            }
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }
    if (showEndPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = rangeEnd.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = 2000..LocalDate.now().year
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        rangeEnd = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                    }
                    showEndPicker = false
                }) { Text("确定", color = primaryColor) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("取消", color = tc.textSecondary) }
            }
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }

    val title = when (mode) {
        MineImportExportMode.Import -> "导入数据"
        MineImportExportMode.Export -> "导出数据"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tc.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = !busy) { onBack() }
            )
            Spacer(Modifier.padding(5.dp))
            Text(
                text = title,
                color = onPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (!state.loggedIn) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = tc.surface),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("请先登录后再使用导入与导出", color = tc.textPrimary, fontSize = 15.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onNeedLogin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = onPrimary
                            )
                        ) { Text("去登录") }
                    }
                }
            }
            return
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (mode) {
                MineImportExportMode.Export -> {
                    Text(
                        "将账单导出为 CSV（UTF-8），可通过系统分享发送到微信、QQ、网盘或邮箱。",
                        color = tc.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tc.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("导出范围", color = tc.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !busy) { exportScope = ExportScope.ALL }
                            ) {
                                RadioButton(
                                    selected = exportScope == ExportScope.ALL,
                                    onClick = { exportScope = ExportScope.ALL },
                                    enabled = !busy,
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                )
                                Text("全部账单", color = tc.textPrimary, fontSize = 15.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !busy) { exportScope = ExportScope.BY_RANGE }
                            ) {
                                RadioButton(
                                    selected = exportScope == ExportScope.BY_RANGE,
                                    onClick = { exportScope = ExportScope.BY_RANGE },
                                    enabled = !busy,
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                )
                                Text("按时间范围", color = tc.textPrimary, fontSize = 15.sp)
                            }
                            if (exportScope == ExportScope.BY_RANGE) {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = tc.divider)
                                Spacer(Modifier.height(8.dp))
                                val df = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { showStartPicker = true },
                                        enabled = !busy
                                    ) {
                                        Text("开始 ${rangeStart.format(df)}", color = primaryColor)
                                    }
                                    Text("至", color = tc.textSecondary, fontSize = 14.sp)
                                    TextButton(
                                        onClick = { showEndPicker = true },
                                        enabled = !busy
                                    ) {
                                        Text("结束 ${rangeEnd.format(df)}", color = primaryColor)
                                    }
                                }
                                Text(
                                    "结束日期包含当天；若开始晚于结束将无法导出。",
                                    color = tc.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                busy = true
                                try {
                                    val from: java.time.LocalDateTime
                                    val toExclusive: java.time.LocalDateTime
                                    when (exportScope) {
                                        ExportScope.ALL -> {
                                            from = LocalDate.of(2000, 1, 1).atStartOfDay()
                                            toExclusive = LocalDate.now().plusDays(1).atStartOfDay()
                                        }
                                        ExportScope.BY_RANGE -> {
                                            if (rangeStart.isAfter(rangeEnd)) {
                                                Toast.makeText(context, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show()
                                                return@launch
                                            }
                                            from = rangeStart.atStartOfDay()
                                            toExclusive = rangeEnd.plusDays(1).atStartOfDay()
                                        }
                                    }
                                    val list = vm.fetchTransactionsForExport(from, toExclusive)
                                    val bytes = BillCsv.exportToCsvBytes(list)
                                    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                                    val name = "账单导出_${
                                        java.time.LocalDateTime.now().format(
                                            DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
                                        )
                                    }.csv"
                                    val file = File(dir, name)
                                    file.writeBytes(bytes)
                                    shareCsvFile(context, file)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "导出失败", Toast.LENGTH_SHORT).show()
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimary,
                            disabledContainerColor = tc.surfaceMuted,
                            disabledContentColor = tc.textSecondary
                        )
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                Modifier.size(22.dp),
                                color = onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("生成并分享", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                MineImportExportMode.Import -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tc.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("说明", color = tc.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "• 不提供固定模板。请上传 UTF-8 编码的 CSV，首行为表头，列名需包含：类型、金额、分类、备注、发生时间（与本应用「导出数据」格式一致）。\n" +
                                    "• 类型填写「支出」或「收入」（也可用 1 / 2）。金额支持小数。\n" +
                                    "• 时间支持如 2026-04-29T12:30:00、2026-04-29 12:30:00 或 2026-04-29。\n" +
                                    "• 若某行的分类名称在您当前账号下不存在，将自动归入「其他支出」或「其他收入」，请提前知悉。",
                                color = tc.textSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { pickCsvLauncher.launch("*/*") },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimary,
                            disabledContainerColor = tc.surfaceMuted,
                            disabledContentColor = tc.textSecondary
                        )
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                Modifier.size(22.dp),
                                color = onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("选择 CSV 文件并导入", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun shareCsvFile(context: android.content.Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_SUBJECT, "账单导出")
        clipData = ClipData.newUri(context.contentResolver, "账单", uri)
    }
    context.startActivity(Intent.createChooser(intent, "分享账单"))
}
