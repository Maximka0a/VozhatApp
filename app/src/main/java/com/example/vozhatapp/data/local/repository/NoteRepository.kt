package com.example.vozhatapp.data.repository

import com.example.vozhatapp.data.local.dao.NoteDao
import com.example.vozhatapp.data.local.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllGeneralNotes(): Flow<List<Note>> {
        return noteDao.getAllGeneralNotes()
    }

    fun getNotesForChild(childId: Long): Flow<List<Note>> {
        return noteDao.getNotesForChild(childId)
    }

    fun getUpcomingReminders(): Flow<List<Note>> {
        return noteDao.getUpcomingReminders(Date())
    }

    suspend fun insertNote(note: Note): Long {
        return withContext(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }
}