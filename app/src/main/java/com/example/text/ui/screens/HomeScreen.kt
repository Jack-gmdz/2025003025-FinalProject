package com.example.text.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.text.ui.theme.*
import com.example.text.viewmodel.CourseListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: CourseListState,
    overdueCount: Int,
    onNavigateToCourses: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课程与作业管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
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
            // 学期信息卡片
            if (state.semesterInfo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "当前学期",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            state.semesterInfo!!.semester,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "第 ${state.semesterInfo!!.currentWeek} 周 / 共 ${state.semesterInfo!!.weekCount} 周",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 快捷入口
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 课程卡片
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToCourses),
                    colors = CardDefaults.cardColors(containerColor = Blue40)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Book, "课程", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("课程", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("${state.courses.size} 门", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                }
                // 作业卡片
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToAssignments),
                    colors = CardDefaults.cardColors(containerColor = Blue30)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Assignment, "作业", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("作业", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("${overdueCount} 项逾期", color = if (overdueCount > 0) RedPriority else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 逾期提醒
            if (overdueCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RedPriority.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, "逾期", tint = RedPriority)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "逾期提醒",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RedPriority
                            )
                            Text(
                                "您有 $overdueCount 项作业已逾期，请尽快处理",
                                style = MaterialTheme.typography.bodySmall,
                                color = RedPriority.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 最近课程
            if (state.courses.isNotEmpty()) {
                Text(
                    "我的课程",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                state.courses.take(5).forEach { course ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (course.teacher.isNotEmpty()) {
                                    Text(course.teacher, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                if (course.schedule.isNotEmpty()) {
                                    Text(course.schedule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            if (course.classroom.isNotEmpty()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(course.classroom) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
