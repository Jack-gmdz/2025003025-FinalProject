package com.example.text.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.text.data.database.AppDatabase
import com.example.text.data.repository.CourseRepository
import com.example.text.datastore.UserPreferences
import com.example.text.ui.screens.*
import com.example.text.viewmodel.*

object Routes {
    const val HOME = "home"
    const val COURSE_LIST = "course_list"
    const val COURSE_DETAIL = "course_detail/{courseId}"
    const val ASSIGNMENT_LIST = "assignment_list"
    const val SETTINGS = "settings"

    fun courseDetail(courseId: Long) = "course_detail/$courseId"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { CourseRepository(db.courseDao(), db.assignmentDao()) }
    val userPreferences = remember { UserPreferences(context) }

    // Shared state
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModel.Factory(repository, userPreferences)
    )
    val courseState by courseViewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        // 首页
        composable(Routes.HOME) {
            val overdueCount by remember {
                derivedStateOf {
                    courseState.courses.fold(0) { acc, _ -> acc }
                }
            }
            HomeScreen(
                state = courseState,
                overdueCount = 0,
                onNavigateToCourses = { navController.navigate(Routes.COURSE_LIST) },
                onNavigateToAssignments = { navController.navigate(Routes.ASSIGNMENT_LIST) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // 课程列表
        composable(Routes.COURSE_LIST) {
            var showAddDialog by remember { mutableStateOf(false) }
            var editTarget by remember { mutableStateOf<com.example.text.data.entity.CourseEntity?>(null) }

            CourseListScreen(
                state = courseState,
                onSearch = courseViewModel::search,
                onAddClick = {
                    courseViewModel.startAddCourse()
                    showAddDialog = true
                },
                onEditClick = { course ->
                    courseViewModel.startEditCourse(course)
                    showAddDialog = true
                },
                onDeleteClick = { courseViewModel.deleteCourse(it) },
                onCourseClick = { courseId ->
                    navController.navigate(Routes.courseDetail(courseId))
                },
                onBack = { navController.popBackStack() },
                showAddDialog = showAddDialog,
                onDismissDialog = {
                    showAddDialog = false
                    courseViewModel.cancelEdit()
                },
                onSaveCourse = { name, teacher, classroom, schedule, credits, semester ->
                    courseViewModel.saveCourse(name, teacher, classroom, schedule, credits, semester)
                    showAddDialog = false
                }
            )
        }

        // 作业列表
        composable(Routes.ASSIGNMENT_LIST) {
            val assignmentViewModel: AssignmentViewModel = viewModel(
                factory = AssignmentViewModel.Factory(repository)
            )
            val assignmentState by assignmentViewModel.state.collectAsState()
            var showAddDialog by remember { mutableStateOf(false) }

            // refresh overdue count in course viewmodel
            LaunchedEffect(assignmentState.overdueAssignments) {
                // update overdue assignments
            }

            AssignmentListScreen(
                state = assignmentState,
                onTabSelect = assignmentViewModel::selectTab,
                onAddClick = {
                    assignmentViewModel.startAddAssignment()
                    showAddDialog = true
                },
                onToggleStatus = assignmentViewModel::toggleStatus,
                onDeleteClick = assignmentViewModel::deleteAssignment,
                onBack = { navController.popBackStack() },
                showAddDialog = showAddDialog,
                onDismissDialog = {
                    showAddDialog = false
                    assignmentViewModel.cancelEdit()
                },
                onSaveAssignment = { courseId, title, desc, dueDate, priority ->
                    assignmentViewModel.saveAssignment(courseId, title, desc, dueDate, priority)
                    showAddDialog = false
                }
            )
        }

        // 课程详情 (作业列表 by course)
        composable(
            route = Routes.COURSE_DETAIL,
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: return@composable
            val assignmentViewModel: AssignmentViewModel = viewModel(
                factory = AssignmentViewModel.Factory(repository)
            )
            val assignmentState by assignmentViewModel.state.collectAsState()
            var showAddDialog by remember { mutableStateOf(false) }

            // This would ideally filter by courseId, reusing assignmentList
            AssignmentListScreen(
                state = assignmentState,
                onTabSelect = assignmentViewModel::selectTab,
                onAddClick = {
                    assignmentViewModel.startAddAssignment()
                    showAddDialog = true
                },
                onToggleStatus = assignmentViewModel::toggleStatus,
                onDeleteClick = assignmentViewModel::deleteAssignment,
                onBack = { navController.popBackStack() },
                showAddDialog = showAddDialog,
                onDismissDialog = {
                    showAddDialog = false
                    assignmentViewModel.cancelEdit()
                },
                onSaveAssignment = { cId, title, desc, dueDate, priority ->
                    assignmentViewModel.saveAssignment(cId, title, desc, dueDate, priority)
                    showAddDialog = false
                }
            )
        }

        // 设置
        composable(Routes.SETTINGS) {
            val scope = rememberCoroutineScope()
            SettingsScreen(
                nickname = userPreferences.nickname,
                reminderDays = userPreferences.reminderDays,
                onNicknameChange = { scope.launch { userPreferences.setNickname(it) } },
                onReminderDaysChange = { scope.launch { userPreferences.setReminderDays(it) } },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
