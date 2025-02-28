package com.example.vozhatapp.data.repository

import com.example.vozhatapp.data.local.dao.AttendanceDao
import com.example.vozhatapp.data.local.entity.Attendance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    fun getAttendanceForEvent(eventId: Long): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForEvent(eventId)
    }

    fun getAttendanceForChild(childId: Long): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForChild(childId)
    }

    fun getChildAttendanceCount(childId: Long): Flow<Int> {
        return attendanceDao.getChildAttendanceCount(childId)
    }

    suspend fun markAttendance(attendance: Attendance): Long {
        return withContext(Dispatchers.IO) {
            attendanceDao.insertAttendance(attendance)
        }
    }

    suspend fun markBulkAttendance(attendances: List<Attendance>) {
        withContext(Dispatchers.IO) {
            attendanceDao.insertAllAttendance(attendances)
        }
    }

    suspend fun updateAttendance(attendance: Attendance) {
        withContext(Dispatchers.IO) {
            attendanceDao.updateAttendance(attendance)
        }
    }

    suspend fun deleteAttendance(attendance: Attendance) {
        withContext(Dispatchers.IO) {
            attendanceDao.deleteAttendance(attendance)
        }
    }
}