package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Note
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE child_id IS NULL ORDER BY created_at DESC")
    fun getAllGeneralNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE child_id = :childId ORDER BY created_at DESC")
    fun getNotesForChild(childId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE type = 1 AND reminder_date >= :now ORDER BY reminder_date ASC")
    fun getUpcomingReminders(now: Date): Flow<List<Note>>

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}