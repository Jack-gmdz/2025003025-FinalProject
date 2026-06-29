package com.example.text.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    nickname: Flow<String>,
    reminderDays: Flow<Int>,
    onNicknameChange: (String) -> Unit,
    onReminderDaysChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    val nicknameValue by nickname.collectAsState(initial = "")
    val reminderValue by reminderDays.collectAsState(initial = 3)
    var editNickname by remember { mutableStateOf(nicknameValue) }
    var editDays by remember { mutableStateOf(reminderValue.toString()) }

    LaunchedEffect(nicknameValue) { editNickname = nicknameValue }
    LaunchedEffect(reminderValue) { editDays = reminderValue.toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 个人信息
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("个人信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editNickname,
                        onValueChange = { editNickname = it },
                        label = { Text("昵称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onNicknameChange(editNickname) }) {
                        Text("保存昵称")
                    }
                }
            }

            // 提醒设置
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("提醒设置", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("截止日提醒 (提前天数)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { if (reminderValue > 1) onReminderDaysChange(reminderValue - 1) }) {
                            Icon(Icons.Default.ArrowBack, "减少")
                        }
                        Text(
                            "$reminderValue 天",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onReminderDaysChange(reminderValue + 1) }) {
                            Icon(Icons.Default.ArrowBack, "增加", modifier = Modifier)
                        }
                    }
                    Slider(
                        value = reminderValue.toFloat(),
                        onValueChange = { onReminderDaysChange(it.toInt()) },
                        valueRange = 1f..14f,
                        steps = 12
                    )
                }
            }

            // 关于
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("关于", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("课程与作业管理 v1.0", style = MaterialTheme.typography.bodyMedium)
                    Text("基于 Kotlin + Jetpack Compose + Room + Retrofit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}
