package com.example.vozhatapp.di

import android.content.Context
import com.example.vozhatapp.data.local.AppDatabase
import com.example.vozhatapp.data.local.dao.*
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // База данных
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // DAO для основных сущностей
    @Singleton
    @Provides
    fun provideChildDao(database: AppDatabase): ChildDao {
        return database.childDao()
    }

    @Singleton
    @Provides
    fun provideEventDao(database: AppDatabase): EventDao {
        return database.eventDao()
    }

    @Singleton
    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }

    @Singleton
    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Singleton
    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Singleton
    @Provides
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Singleton
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    // DAO для связей между таблицами
    @Singleton
    @Provides
    fun provideChildWithDetailsDao(database: AppDatabase): ChildWithDetailsDao {
        return database.childWithDetailsDao()
    }

    @Singleton
    @Provides
    fun provideEventWithAttendanceDao(database: AppDatabase): EventWithAttendanceDao {
        return database.eventWithAttendanceDao()
    }

    // Репозитории
    @Singleton
    @Provides
    fun provideChildRepository(
        childDao: ChildDao,
        childWithDetailsDao: ChildWithDetailsDao
    ): ChildRepository {
        return ChildRepository(childDao, childWithDetailsDao)
    }

    @Singleton
    @Provides
    fun provideEventRepository(
        eventDao: EventDao,
        eventWithAttendanceDao: EventWithAttendanceDao
    ): EventRepository {
        return EventRepository(eventDao, eventWithAttendanceDao)
    }

    @Singleton
    @Provides
    fun provideAttendanceRepository(
        attendanceDao: AttendanceDao
    ): AttendanceRepository {
        return AttendanceRepository(attendanceDao)
    }

    @Singleton
    @Provides
    fun provideNoteRepository(
        noteDao: NoteDao
    ): NoteRepository {
        return NoteRepository(noteDao)
    }

    @Singleton
    @Provides
    fun provideAchievementRepository(
        achievementDao: AchievementDao
    ): AchievementRepository {
        return AchievementRepository(achievementDao)
    }

    @Singleton
    @Provides
    fun provideGameRepository(
        gameDao: GameDao
    ): GameRepository {
        return GameRepository(gameDao)
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository {
        return UserRepository(userDao)
    }
}