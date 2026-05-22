package com.wyjqwy.app.ui.main

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wyjqwy.app.R
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import com.wyjqwy.app.ui.theme.themeColors
import kotlinx.coroutines.delay

private enum class AuthMode { LOGIN, REGISTER }

@Composable
fun BookkeepingLoginScreen(
    state: AppUiState,
    vm: AppViewModel,
    onBack: (() -> Unit)? = null
) {
    val appTitle = stringResource(R.string.app_display_name)
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf("") }
    var floatingTip by remember { mutableStateOf("") }
    var floatingTipSeq by remember { mutableStateOf(0) }
    val primaryColor = rememberThemePrimaryColor()
    val tc = themeColors()
    val onPrimary = contentColorFor(primaryColor)
    LaunchedEffect(state.message) {
        val msg = state.message.trim()
        if (msg.isBlank()) return@LaunchedEffect
        if (msg.contains("注册成功")) {
            mode = AuthMode.LOGIN
            localError = ""
            password = ""
            floatingTip = "注册成功，请登录"
            floatingTipSeq++
            vm.clearUiMessage()
        } else {
            floatingTip = msg
            floatingTipSeq++
            vm.clearUiMessage()
        }
    }
    LaunchedEffect(floatingTipSeq) {
        val msg = floatingTip.trim()
        if (msg.isNotBlank()) {
            delay(1800)
            if (floatingTip == msg) {
                floatingTip = ""
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().background(primaryColor)) {
                Column(
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (onBack != null) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "返回",
                                tint = onPrimary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onBack() }
                            )
                        } else {
                            Spacer(Modifier.size(24.dp))
                        }
                        Text(
                            text = if (mode == AuthMode.LOGIN) "登录" else "注册",
                            style = MaterialTheme.typography.titleLarge,
                            color = onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(primaryColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.5f) // 🌟 核心：1.5f 代表放大 1.5 倍。你可以试着调成 1.2f 或 1.8f
                            )
                        }
                        Spacer(Modifier.size(10.dp))
                        Text(
                            text = appTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = if (mode == AuthMode.LOGIN) "使用手机号密码登录" else "请完成账号注册",
                        style = MaterialTheme.typography.bodyLarge,
                        color = onPrimary.copy(alpha = 0.92f),
                        modifier = Modifier.padding(top = 18.dp)
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .background(tc.background)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it.filter { ch -> ch.isDigit() }.take(11)
                    localError = ""
                    vm.clearUiMessage()
                },
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("请输入手机号") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = tc.divider,
                    focusedContainerColor = tc.surface,
                    unfocusedContainerColor = tc.surface
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    localError = ""
                    vm.clearUiMessage()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("请输入密码") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "切换密码显示",
                            tint = tc.textSecondary
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = tc.divider,
                    focusedContainerColor = tc.surface,
                    unfocusedContainerColor = tc.surface
                )
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                )
                Text(
                    text = "已阅读并同意《用户协议》和《隐私协议》",
                    color = tc.textSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(12.dp))
            val actionEnabled = !state.loading
            Button(
                onClick = {
                    when {
                        !phone.matches(Regex("^1[3-9]\\d{9}$")) -> {
                            floatingTip = "请输入正确的11位手机号"
                            floatingTipSeq++
                        }
                        mode == AuthMode.REGISTER && password.length < 7 -> {
                            floatingTip = "密码至少7位"
                            floatingTipSeq++
                        }
                        password.isBlank() -> {
                            floatingTip = "请输入密码"
                            floatingTipSeq++
                        }
                        !agreed -> {
                            floatingTip = "请先同意协议"
                            floatingTipSeq++
                        }
                        else -> {
                            localError = ""
                            if (mode == AuthMode.LOGIN) {
                                vm.login(phone, password)
                            } else {
                                vm.register(phone, password)
                            }
                        }
                    }
                },
                enabled = actionEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == AuthMode.LOGIN) primaryColor else tc.surfaceMuted,
                    contentColor = if (mode == AuthMode.LOGIN) contentColorFor(primaryColor) else tc.textPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (mode == AuthMode.LOGIN) "登录" else "注册",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mode == AuthMode.LOGIN) {
                    Text(
                        "注册账号",
                        color = tc.textSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable {
                            mode = AuthMode.REGISTER
                            localError = ""
                            vm.clearUiMessage()
                        }
                    )
                } else {
                    Text(
                        "返回登录",
                        color = tc.textSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable {
                            mode = AuthMode.LOGIN
                            localError = ""
                            vm.clearUiMessage()
                        }
                    )
                }
            }
        }
        }
        if (floatingTip.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = floatingTip,
                    color = tc.expense,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
