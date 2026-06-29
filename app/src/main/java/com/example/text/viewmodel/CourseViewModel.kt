package com.example.text.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.text.data.entity.CourseEntity
import com.example.text.data.repository.CourseRepository
import com.example.text.datastore.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CourseListState(
    val uiState: UiState<List<CourseEntity>> = UiState.Idle,
    val courses: List<CourseEntity> = emptyList(),
    val suggestions: List<com.example.text.data.network.dto.CourseSuggestionDto> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isEditMode: Boolean = false,
    val editingCourse: CourseEntity? = null,
    val selectedSemester: String = "全部",
    val semesterInfo: com.example.text.data.network.dto.SemesterInfoDto? = null
)

class CourseViewModel(
    private val repository: CourseRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(CourseListState())
    val state: StateFlow<CourseListState> = _state.asStateFlow()

    init {
        loadCourses()
        loadSemesterInfo()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            repository.getAllCourses().collect { courses ->
                _state.update { it.copy(courses = courses, uiState = UiState.Success(courses)) }
            }
        }
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length < 2) return
        _state.update { it.copy(isSearching = true) }
        viewModelScope.launch {
            repository.searchCourseSuggestions(query)
                .onSuccess { suggestions ->
                    _state.update { it.copy(suggestions = suggestions, isSearching = false) }
                }
                .onFailure {
                    _state.update { it.copy(isSearching = false) }
                }
            if (query.length >= 2) {
                userPreferences.addRecentSearch(query)
            }
        }
    }

    fun startAddCourse() {
        _state.update {
            it.copy(
                isEditMode = true,
                editingCourse = CourseEntity(name = "")
            )
        }
    }

    fun startEditCourse(course: CourseEntity) {
        _state.update { it.copy(isEditMode = true, editingCourse = course) }
    }

    fun cancelEdit() {
        _state.update { it.copy(isEditMode = false, editingCourse = null) }
    }

    fun saveCourse(
        name: String, teacher: String, classroom: String,
        schedule: String, credits: Int, semester: String
    ) {
        val editing = _state.value.editingCourse ?: return
        viewModelScope.launch {
            repository.saveCourse(
                editing.copy(
                    name = name, teacher = teacher, classroom = classroom,
                    schedule = schedule, credits = credits, semester = semester
                )
            )
            _state.update { it.copy(isEditMode = false, editingCourse = null) }
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    fun selectSemester(semester: String) {
        _state.update { it.copy(selectedSemester = semester) }
    }

    private fun loadSemesterInfo() {
        viewModelScope.launch {
            repository.getSemesterInfo().onSuccess { info ->
                _state.update { it.copy(semesterInfo = info) }
            }
        }
    }

    class Factory(
        private val repository: CourseRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CourseViewModel(repository, userPreferences) as T
        }
    }
}
