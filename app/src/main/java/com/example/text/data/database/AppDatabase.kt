package com.example.text.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.text.data.dao.AssignmentDao
import com.example.text.data.dao.CourseDao
import com.example.text.data.entity.AssignmentEntity
import com.example.text.data.entity.CourseEntity

@Database(
    entities = [CourseEntity::class, AssignmentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "course_management_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
