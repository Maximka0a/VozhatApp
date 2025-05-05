package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE child_id = :childId ORDER BY date DESC")
    fun getAchievementsForChild(childId: Long): Flow<List<Achievement>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH) // Добавляем это для подавления предупреждения
    @Query("""
    SELECT c.id, c.name, c.lastName, c.squadName, c.photo_url as photoUrl, SUM(a.points) as totalPoints 
        FROM children c
        LEFT JOIN achievements a ON c.id = a.child_id
        GROUP BY c.id
        ORDER BY totalPoints DESC
    """)
    fun getChildrenRanking(): Flow<List<ChildWithPoints>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement): Long

    @Query("SELECT * FROM achievements WHERE id = :achievementId LIMIT 1")
    suspend fun getAchievementById(achievementId: Long): Achievement?

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Delete
    suspend fun deleteAchievement(achievement: Achievement)

    data class ChildWithPoints(
        val id: Long,
        val name: String,
        val lastName: String,
        val squadName: String,
        val totalPoints: Int?,
        val photoUrl: String? // Поле должно соответствовать имени в запросе
    )
}