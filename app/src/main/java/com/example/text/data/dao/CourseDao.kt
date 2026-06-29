package com.example.text.data.dao

import androidx.room.*
import com.example.text.data.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE semester = :semester ORDER BY name ASC")
    fun getCoursesBySemester(semester: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE name LIKE '%' || :query || '%' OR teacher LIKE '%' || :query || '%'")
    suspend fun searchCourses(query: String): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCount(): Int
}
