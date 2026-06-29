package com.example.text.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.text.data.entity.AssignmentEntity
import com.example.text.data.repository.CourseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AssignmentListState(
    val uiState: UiState<List<AssignmentEntity>> = UiState.Idle,
    val assignments: List<AssignmentEntity> = emptyList(),
    val selectedTab: String = "全部",
    val isEditMode: Boolean = false,
    val editingAssignment: AssignmentEntity? = null,
    val courses: List<com.example.text.data.entity.CourseEntity> = emptyList(),
    val overdueAssignments: List<AssignmentEntity> = emptyList()
)

class AssignmentViewModel(
    private val repository: CourseRepository,
    courseId: Long = 0L
) : ViewModel() {

    private val _state = MutableStateFlow(AssignmentListState())
    val state: StateFlow<AssignmentListState> = _state.asStateFlow()

    init {
        loadCourses()
        loadAssignments()
        checkOverdue()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            repository.getAllCourses().collect { courses ->
                _state.update { it.copy(courses = courses) }
            }
        }
    }

    fun selectTab(tab: String) {
        _state.update { it.copy(selectedTab = tab) }
        loadAssignments()
    }

    private fun loadAssignments() {
        viewModelScope.launch {
            val flow = when (_state.value.selectedTab) {
                "待提交" -> repository.getAssignmentsByStatus("pending")
                "已提交" -> repository.getAssignmentsByStatus("submitted")
                "已批阅" -> repository.getAssignmentsByStatus("graded")
                else -> repository.getAllAssignments()
            }
            flow.collect { assignments ->
                _state.update { it.copy(assignments = assignments, uiState = UiState.Success(assignments)) }
            }
        }
    }

    private fun checkOverdue() {
        viewModelScope.launch {
            val overdue = repository.getOverdueAssignments(System.currentTimeMillis())
            _state.update { it.copy(overdueAssignments = overdue) }
        }
    }

    fun startAddAssignment() {
        _state.update {
            it.copy(
                isEditMode = true,
                editingAssignment = AssignmentEntity(
                    courseId = 0L,
                    title = "",
                    dueDate = System.currentTimeMillis() + 7 * 24 * 3600 * 1000
                )
            )
        }
    }

    fun startEditAssignment(assignment: AssignmentEntity) {
        _state.update { it.copy(isEditMode = true, editingAssignment = assignment) }
    }

    fun cancelEdit() {
        _state.update { it.copy(isEditMode = false, editingAssignment = null) }
    }

    fun saveAssignment(
        courseId: Long, title: String, description: String,
        dueDate: Long, priority: Int
    ) {
        val editing = _state.value.editingAssignment ?: return
        viewModelScope.launch {
            repository.saveAssignment(
                editing.copy(
                    courseId = courseId, title = title, description = description,
                    dueDate = dueDate, priority = priority
                )
            )
            _state.update { it.copy(isEditMode = false, editingAssignment = null) }
            checkOverdue()
        }
    }

    fun toggleStatus(assignment: AssignmentEntity) {
        viewModelScope.launch {
            val newStatus = when (assignment.status) {
                "pending" -> "submitted"
                "submitted" -> "graded"
                else -> "pending"
            }
            repository.saveAssignment(assignment.copy(status = newStatus))
        }
    }

    fun gradeAssignment(assignment: AssignmentEntity, grade: String, feedback: String) {
        viewModelScope.launch {
            repository.saveAssignment(
                assignment.copy(status = "graded", grade = grade, feedback = feedback)
            )
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    fun formatDueDate(dueDate: Long): String {
        val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(dueDate))
    }

    fun isOverdue(dueDate: Long): Boolean {
        return dueDate < System.currentTimeMillis()
    }

    class Factory(
        private val repository: CourseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssignmentViewModel(repository) as T
        }
    }
}
