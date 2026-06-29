package com.example.text.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val teacher: String = "",
    val classroom: String = "",
    val schedule: String = "",       // e.g. "周一 9:00-10:30"
    val credits: Int = 0,
    val semester: String = ""        // e.g. "2025-2026-1"
)
