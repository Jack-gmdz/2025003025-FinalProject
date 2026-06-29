package com.example.text.data.network.dto

data class CourseDto(
    val id: String,
    val name: String,
    val teacher: String,
    val credits: Int,
    val semester: String
)

data class CourseSuggestionDto(
    val id: String,
    val name: String,
    val teacher: String,
    val credits: Int,
    val description: String
)

data class SemesterInfoDto(
    val semester: String,
    val startDate: String,
    val endDate: String,
    val weekCount: Int,
    val currentWeek: Int
)
