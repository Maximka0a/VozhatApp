package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE eventId = :eventId")
    fun getAttendanceForEvent(eventId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE childId = :childId")
    fun getAttendanceForChild(childId: Long): Flow<List<Attendance>>

    @Query("SELECT COUNT(*) FROM attendance WHERE childId = :childId AND isPresent = 1")
    fun getChildAttendanceCount(childId: Long): Flow<Int>

    @Insert
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert
    suspend fun insertAllAttendance(attendances: List<Attendance>)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
}