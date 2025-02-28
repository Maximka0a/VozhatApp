package com.example.vozhatapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.vozhatapp.data.local.dao.*
import com.example.vozhatapp.data.local.entity.*

@Database(
    entities = [
        Child::class,
        Event::class,
        Attendance::class,
        User::class,
        Achievement::class,
        Note::class,
        Game::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childDao(): ChildDao
    abstract fun eventDao(): EventDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun userDao(): UserDao
    abstract fun achievementDao(): AchievementDao
    abstract fun noteDao(): NoteDao
    abstract fun gameDao(): GameDao

    abstract fun childWithDetailsDao(): ChildWithDetailsDao
    abstract fun eventWithAttendanceDao(): EventWithAttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vozhat_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}