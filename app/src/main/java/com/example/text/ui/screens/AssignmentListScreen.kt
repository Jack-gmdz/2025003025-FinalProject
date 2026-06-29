package com.example.text.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.text.data.entity.AssignmentEntity
import com.example.text.ui.theme.*
import com.example.text.viewmodel.AssignmentListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentListScreen(
    state: AssignmentListState,
    onTabSelect: (String) -> Unit,
    onAddClick: () -> Unit,
    onToggleStatus: (AssignmentEntity) -> Unit,
    onDeleteClick: (AssignmentEntity) -> Unit,
    onBack: () -> Unit,
    // Add dialog
    showAddDialog: Boolean,
    onDismissDialog: () -> Unit,
    onSaveAssignment: (Long, String, String, Long, Int) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf<AssignmentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作业管理") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "添加作业")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tab 筛选
            val tabs = listOf("全部", "待提交", "已提交", "已批阅")
            TabRow(selectedTabIndex = tabs.indexOf(state.selectedTab).coerceAtLeast(0)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { onTabSelect(tab) },
                        text = { Text(tab) }
                    )
                }
            }

            if (state.assignments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, "无作业", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无作业", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.assignments, key = { it.id }) { assignment ->
                        AssignmentCard(
                            assignment = assignment,
                            courseName = state.courses.find { it.id == assignment.courseId }?.name ?: "未知课程",
                            isOverdue = assignment.dueDate < System.currentTimeMillis() && assignment.status == "pending",
                            onToggleStatus = { onToggleStatus(assignment) },
                            onDelete = { showDeleteConfirm = assignment }
                        )
                    }
                }
            }
        }
    }

    // 删除确认
    showDeleteConfirm?.let { assignment ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除作业「${assignment.title}」吗？") },
            confirmButton = {
                TextButton(onClick = { onDeleteClick(assignment); showDeleteConfirm = null }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") }
            }
        )
    }

    // 添加对话框
    if (showAddDialog) {
        AddEditAssignmentDialog(
            assignment = state.editingAssignment,
            courses = state.courses,
            onDismiss = onDismissDialog,
            onSave = onSaveAssignment
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignmentCard(
    assignment: AssignmentEntity,
    courseName: String,
    isOverdue: Boolean,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (assignment.priority) {
        2 -> RedPriority
        1 -> OrangePriority
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }
    val statusText = when (assignment.status) {
        "pending" -> "待提交"
        "submitted" -> "已提交"
        "graded" -> "已批阅"
        else -> assignment.status
    }
    val statusColor = when (assignment.status) {
        "pending" -> if (isOverdue) RedPriority else OrangePriority
        "submitted" -> Blue40
        "graded" -> GreenStatus
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(assignment.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(courseName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (assignment.priority > 0) {
                            Icon(
                                Icons.Default.Flag, null,
                                tint = priorityColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                statusText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            if (assignment.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(assignment.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = if (isOverdue) RedPriority else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "截止: ${sdf.format(java.util.Date(assignment.dueDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) RedPriority else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Row {
                    TextButton(onClick = onToggleStatus) {
                        Text(when (assignment.status) {
                            "pending" -> "标记已提交"
                            "submitted" -> "标记已批阅"
                            else -> "重置"
                        }, style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "删除", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                    }
                }
            }

            // 已批阅显示成绩
            if (assignment.status == "graded" && assignment.grade.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("成绩: ${assignment.grade}", style = MaterialTheme.typography.labelMedium, color = GreenStatus, fontWeight = FontWeight.Bold)
                if (assignment.feedback.isNotEmpty()) {
                    Text("反馈: ${assignment.feedback}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssignmentDialog(
    assignment: AssignmentEntity?,
    courses: List<com.example.text.data.entity.CourseEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, String, String, Long, Int) -> Unit
) {
    var courseId by remember { mutableStateOf(assignment?.courseId ?: courses.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf(assignment?.title ?: "") }
    var description by remember { mutableStateOf(assignment?.description ?: "") }
    var dueDate by remember { mutableStateOf(assignment?.dueDate ?: (System.currentTimeMillis() + 7 * 24 * 3600 * 1000)) }
    var priority by remember { mutableStateOf(assignment?.priority ?: 0) }
    var titleError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (assignment?.id == 0L || assignment?.id == null) "添加作业" else "编辑作业") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 课程选择
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = courses.find { it.id == courseId }?.name ?: "选择课程",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("关联课程") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.name) },
                                onClick = { courseId = course.id; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("作业标题 *") },
                    isError = titleError,
                    supportingText = if (titleError) {{ Text("请输入作业标题") }} else null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("作业描述") },
                    maxLines = 3
                )
                OutlinedTextField(
                    value = sdf.format(java.util.Date(dueDate)),
                    onValueChange = { /* date picker simplified */ },
                    label = { Text("截止日期") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true
                )

                // 优先级
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "普通", 1 to "重要", 2 to "紧急").forEach { (p, label) ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@TextButton }
                    onSave(courseId, title.trim(), description.trim(), dueDate, priority)
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
