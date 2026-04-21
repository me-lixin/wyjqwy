package com.wyjqwy.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookColors

@Composable
fun BookkeepingLoginScreen(state: AppUiState, vm: AppViewModel) {
    var username by remember { mutableStateOf("test01") }
    var password by remember { mutableStateOf("123456") }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().background(BookColors.Main)) {
            Column(
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Text(
                    "鲨鱼记账风格",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = BookColors.TextBlack
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "登录后同步你的账本",
                    fontSize = 14.sp,
                    color = BookColors.TextBlack.copy(alpha = 0.85f)
                )
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .background(BookColors.Background)
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(BookColors.White, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BookColors.MainDark,
                        unfocusedBorderColor = BookColors.Line,
                        focusedLabelColor = BookColors.TextGray
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BookColors.MainDark,
                        unfocusedBorderColor = BookColors.Line
                    )
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { vm.login(username, password) },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BookColors.Main,
                        contentColor = BookColors.TextBlack
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("登录", fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { vm.register(username, password) },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("注册", color = BookColors.TextBlack)
                }
            }
            if (state.message.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(state.message, color = BookColors.TextGray, fontSize = 13.sp)
            }
        }
    }
}
