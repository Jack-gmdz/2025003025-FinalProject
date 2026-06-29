package com.example.text.data.repository

import com.example.text.data.dao.AssignmentDao
import com.example.text.data.dao.CourseDao
import com.example.text.data.entity.AssignmentEntity
import com.example.text.data.entity.CourseEntity
import com.example.text.data.network.NetworkDataSource
import com.example.text.data.network.dto.CourseSuggestionDto
import com.example.text.data.network.dto.SemesterInfoDto
import kotlinx.coroutines.flow.Flow

class CourseRepository(
    private val courseDao: CourseDao,
    private val assignmentDao: AssignmentDao
) {
    // ===== Courses =====
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun getCoursesBySemester(semester: String): Flow<List<CourseEntity>> =
        courseDao.getCoursesBySemester(semester)

    suspend fun searchCourses(query: String): List<CourseEntity> =
        courseDao.searchCourses(query)

    suspend fun getCourseById(id: Long): CourseEntity? = courseDao.getCourseById(id)

    suspend fun saveCourse(course: CourseEntity): Long =
        if (course.id == 0L) courseDao.insert(course)
        else { courseDao.update(course); course.id }

    suspend fun deleteCourse(course: CourseEntity) = courseDao.delete(course)

    // ===== Assignments =====
    fun getAssignmentsByCourse(courseId: Long): Flow<List<AssignmentEntity>> =
        assignmentDao.getAssignmentsByCourse(courseId)

    fun getAllAssignments(): Flow<List<AssignmentEntity>> =
        assignmentDao.getAllAssignments()

    fun getAssignmentsByStatus(status: String): Flow<List<AssignmentEntity>> =
        assignmentDao.getAssignmentsByStatus(status)

    suspend fun saveAssignment(assignment: AssignmentEntity): Long =
        if (assignment.id == 0L) assignmentDao.insert(assignment)
        else { assignmentDao.update(assignment); assignment.id }

    suspend fun deleteAssignment(assignment: AssignmentEntity) =
        assignmentDao.delete(assignment)

    suspend fun getOverdueAssignments(now: Long): List<AssignmentEntity> =
        assignmentDao.getOverdueAssignments(now)

    // ===== Network =====
    suspend fun searchCourseSuggestions(query: String): Result<List<CourseSuggestionDto>> =
        NetworkDataSource.searchCourseSuggestions(query)

    suspend fun getSemesterInfo(): Result<SemesterInfoDto> =
        NetworkDataSource.getSemesterInfo()
}
