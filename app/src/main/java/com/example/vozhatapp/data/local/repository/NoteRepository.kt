package com.example.vozhatapp.data.repository

import com.example.vozhatapp.data.local.dao.NoteDao
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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

    fun getTodayReminders(): Flow<List<Note>> {
        val currentTimeMillis = System.currentTimeMillis()
        val startOfDay = DateUtils.getStartOfDay(currentTimeMillis)
        val endOfDay = DateUtils.getEndOfDay(currentTimeMillis)
        return noteDao.getTodayReminders(startOfDay, endOfDay)
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