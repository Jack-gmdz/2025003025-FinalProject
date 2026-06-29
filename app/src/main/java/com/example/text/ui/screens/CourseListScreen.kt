package com.example.text.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.text.data.entity.CourseEntity
import com.example.text.data.network.dto.CourseSuggestionDto
import com.example.text.ui.theme.Blue40
import com.example.text.viewmodel.CourseListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    state: CourseListState,
    onSearch: (String) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (CourseEntity) -> Unit,
    onDeleteClick: (CourseEntity) -> Unit,
    onCourseClick: (Long) -> Unit,
    onBack: () -> Unit,
    // Add/Edit dialog
    showAddDialog: Boolean,
    onDismissDialog: () -> Unit,
    onSaveCourse: (String, String, String, String, Int, String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf<CourseEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课程列表") },
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
                Icon(Icons.Default.Add, "添加课程")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // 搜索框
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearch,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索课程...") },
                leadingIcon = { Icon(Icons.Default.Search, "搜索") },
                trailingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                },
                singleLine = true
            )

            // 网络搜索建议
            if (state.suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("搜索结果", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        state.suggestions.forEach { suggestion ->
                            ResultItem(suggestion) {
                                onSaveCourse(suggestion.name, suggestion.teacher, "", "", suggestion.credits, "")
                                onSearch("")
                            }
                        }
                    }
                }
            }

            // 学期筛选
            SemesterFilter(
                selected = state.selectedSemester,
                onSelect = { /* semester filter */ }
            )

            // 课程列表
            if (state.courses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Book, "无课程", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无课程", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("点击右下角 + 添加课程", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.courses, key = { it.id }) { course ->
                        CourseCard(
                            course = course,
                            onClick = { onCourseClick(course.id) },
                            onEdit = { onEditClick(course) },
                            onDelete = { showDeleteConfirm = course }
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    showDeleteConfirm?.let { course ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除课程「${course.name}」吗？相关作业也将被删除。") },
            confirmButton = {
                TextButton(onClick = { onDeleteClick(course); showDeleteConfirm = null }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") }
            }
        )
    }

    // 添加/编辑对话框
    if (showAddDialog) {
        AddEditCourseDialog(
            course = state.editingCourse,
            onDismiss = onDismissDialog,
            onSave = onSaveCourse
        )
    }
}

@Composable
private fun ResultItem(suggestion: CourseSuggestionDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.School, null, tint = Blue40)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(suggestion.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("${suggestion.teacher} · ${suggestion.credits}学分", style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.Add, "添加")
    }
}

@Composable
fun SemesterFilter(selected: String, onSelect: (String) -> Unit) {
    val semesters = listOf("全部", "2025-2026-1", "2025-2026-2", "2024-2025-2")
    ScrollableTabRow(
        selectedTabIndex = semesters.indexOf(selected).coerceAtLeast(0),
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 16.dp
    ) {
        semesters.forEach { semester ->
            Tab(
                selected = selected == semester,
                onClick = { onSelect(semester) },
                text = { Text(semester) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseCard(
    course: CourseEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Book, null, tint = Blue40, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(course.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (course.teacher.isNotEmpty()) {
                    Text(course.teacher, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                if (course.schedule.isNotEmpty()) {
                    Text(course.schedule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                if (course.classroom.isNotEmpty()) {
                    Text("教室: ${course.classroom}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                if (course.semester.isNotEmpty()) {
                    Text(course.semester, style = MaterialTheme.typography.labelSmall, color = Blue40)
                }
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", tint = Blue40)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ===== 预设数据 =====
private val COURSE_NAMES = listOf(
    "数据结构与算法", "操作系统原理", "计算机网络",
    "编译原理", "数据库系统", "软件工程",
    "人工智能基础", "机器学习", "计算机组成原理",
    "Web开发技术", "移动应用开发", "计算机图形学",
    "高等数学", "线性代数", "大学物理", "大学英语"
)
private val TEACHER_SURNAMES = listOf(
    "张", "李", "王", "赵", "陈", "刘", "周", "吴",
    "杨", "黄", "孙", "钱", "何", "林", "郭", "徐",
    "胡", "高", "马", "罗"
)
private val CLASSROOMS = listOf(
    "科技楼201", "科技楼301", "科技楼401",
    "主楼101", "主楼102", "主楼203",
    "综合楼502", "综合楼603",
    "实验楼A101", "实验楼B205"
)
private val SCHEDULES = listOf(
    "周一 8:00-9:40", "周一 10:00-11:40",
    "周二 8:00-9:40", "周二 10:00-11:40",
    "周三 8:00-9:40", "周三 10:00-11:40", "周三 14:00-15:40",
    "周四 8:00-9:40", "周四 14:00-15:40",
    "周五 8:00-9:40", "周五 10:00-11:40"
)
private val CREDITS = listOf("1", "2", "3", "4", "5", "6")
private val SEMESTERS = listOf("2025-2026-2", "2025-2026-1", "2024-2025-2", "2024-2025-1")

private const val CUSTOM_OPTION = "自定义..."

// ===== 通用下拉+自定义输入组件 =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownWithCustom(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var isCustom by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }

    // 判断当前值是否来自预设
    val isPreset = !isCustom && (selected.isNotEmpty() && options.contains(selected) || selected.isEmpty())
    val displayValue = when {
        isCustom -> customText
        isPreset && selected.isNotEmpty() -> selected
        else -> ""
    }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded && !isCustom,
            onExpandedChange = { expanded = !expanded && !isCustom }
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                isError = isError,
                supportingText = supportingText,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            isCustom = false
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(CUSTOM_OPTION, color = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        isCustom = true
                        customText = ""
                        onSelect("")
                        expanded = false
                    }
                )
            }
        }

        // 自定义输入框
        AnimatedVisibility(visible = isCustom) {
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it; onSelect(it) },
                label = { Text("输入${label}") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(CUSTOM_OPTION, color = MaterialTheme.colorScheme.primary) },
                onClick = { onSelect(CUSTOM_OPTION); expanded = false }
            )
        }
    }
}

// ===== 课程添加/编辑对话框 =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCourseDialog(
    course: com.example.text.data.entity.CourseEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var teacher by remember { mutableStateOf(course?.teacher ?: "") }
    var classroom by remember { mutableStateOf(course?.classroom ?: "") }
    var schedule by remember { mutableStateOf(course?.schedule ?: "") }
    var credits by remember { mutableStateOf((course?.credits ?: 3).toString()) }
    var semester by remember { mutableStateOf(course?.semester ?: "2025-2026-2") }
    var nameError by remember { mutableStateOf(false) }

    // 学分自定义
    var creditsExpanded by remember { mutableStateOf(false) }
    var isCreditsCustom by remember { mutableStateOf(false) }
    var customCredits by remember { mutableStateOf("") }

    // 学期自定义
    var semesterExpanded by remember { mutableStateOf(false) }
    var isSemesterCustom by remember { mutableStateOf(false) }
    var customSemester by remember { mutableStateOf("") }

    // 初始状态下，如果已有值且不在预设中，进入自定义模式
    LaunchedEffect(Unit) {
        if (name.isNotEmpty() && !COURSE_NAMES.contains(name)) { /* already custom */ }
        if (teacher.isNotEmpty() && !TEACHER_SURNAMES.contains(teacher)) { /* already custom */ }
        if (classroom.isNotEmpty() && !CLASSROOMS.contains(classroom)) { /* already custom */ }
        if (schedule.isNotEmpty() && !SCHEDULES.contains(schedule)) { /* already custom */ }
    }

    val teacherOptions = TEACHER_SURNAMES.map { "${it}老师" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (course?.id == 0L || course?.id == null) "添加课程" else "编辑课程") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 480.dp)
            ) {
                // 课程名称 - 下拉+自定义
                DropdownWithCustom(
                    label = "课程名称 *",
                    options = COURSE_NAMES,
                    selected = name,
                    onSelect = { name = it; nameError = false },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("请选择或输入课程名称") }} else null
                )

                // 授课教师 - 下拉+自定义
                DropdownWithCustom(
                    label = "授课教师",
                    options = teacherOptions,
                    selected = teacher,
                    onSelect = { teacher = it }
                )

                // 上课教室 - 下拉+自定义
                DropdownWithCustom(
                    label = "上课教室",
                    options = CLASSROOMS,
                    selected = classroom,
                    onSelect = { classroom = it }
                )

                // 上课时间 - 下拉+自定义
                DropdownWithCustom(
                    label = "上课时间",
                    options = SCHEDULES,
                    selected = schedule,
                    onSelect = { schedule = it }
                )

                // 学分 - 下拉+自定义
                val creditDisplay = when {
                    isCreditsCustom -> customCredits
                    credits.isNotEmpty() && CREDITS.contains(credits) -> credits
                    else -> ""
                }
                ExposedDropdownMenuBox(
                    expanded = creditsExpanded && !isCreditsCustom,
                    onExpandedChange = { creditsExpanded = !creditsExpanded && !isCreditsCustom }
                ) {
                    OutlinedTextField(
                        value = creditDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("学分") },
                        trailingIcon = {
                            IconButton(onClick = { creditsExpanded = !creditsExpanded }) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = creditsExpanded)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = creditsExpanded, onDismissRequest = { creditsExpanded = false }) {
                        CREDITS.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c} 学分") },
                                onClick = { isCreditsCustom = false; credits = c; creditsExpanded = false }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(CUSTOM_OPTION, color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                isCreditsCustom = true
                                customCredits = ""
                                credits = ""
                                creditsExpanded = false
                            }
                        )
                    }
                }
                AnimatedVisibility(visible = isCreditsCustom) {
                    OutlinedTextField(
                        value = customCredits,
                        onValueChange = {
                            customCredits = it.filter { c -> c.isDigit() }
                            credits = customCredits
                        },
                        label = { Text("输入学分") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 学期 - 下拉+自定义
                val semesterDisplay = when {
                    isSemesterCustom -> customSemester
                    semester.isNotEmpty() && SEMESTERS.contains(semester) -> semester
                    else -> ""
                }
                ExposedDropdownMenuBox(
                    expanded = semesterExpanded && !isSemesterCustom,
                    onExpandedChange = { semesterExpanded = !semesterExpanded && !isSemesterCustom }
                ) {
                    OutlinedTextField(
                        value = semesterDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("学期") },
                        trailingIcon = {
                            IconButton(onClick = { semesterExpanded = !semesterExpanded }) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = semesterExpanded, onDismissRequest = { semesterExpanded = false }) {
                        SEMESTERS.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { isSemesterCustom = false; semester = s; semesterExpanded = false }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(CUSTOM_OPTION, color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                isSemesterCustom = true
                                customSemester = ""
                                semester = ""
                                semesterExpanded = false
                            }
                        )
                    }
                }
                AnimatedVisibility(visible = isSemesterCustom) {
                    OutlinedTextField(
                        value = customSemester,
                        onValueChange = { customSemester = it; semester = customSemester },
                        label = { Text("输入学期") },
                        placeholder = { Text("如: 2025-2026-2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@TextButton }
                    onSave(
                        name.trim(), teacher.trim(), classroom.trim(),
                        schedule.trim(), credits.toIntOrNull() ?: 0, semester.trim()
                    )
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
