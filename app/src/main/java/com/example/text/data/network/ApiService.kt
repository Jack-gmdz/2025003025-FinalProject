package com.example.text.data.network

import com.example.text.data.network.dto.CourseDto
import com.example.text.data.network.dto.CourseSuggestionDto
import com.example.text.data.network.dto.SemesterInfoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/courses/suggestions")
    suspend fun searchCourseSuggestions(@Query("q") query: String): List<CourseSuggestionDto>

    @GET("api/semester/info")
    suspend fun getSemesterInfo(): SemesterInfoDto

    @GET("api/courses")
    suspend fun getAllCourses(): List<CourseDto>
}
