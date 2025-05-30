package com.example.vozhatapp.data.repository

import com.example.vozhatapp.data.local.dao.AchievementDao
import com.example.vozhatapp.data.local.entity.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    suspend fun getAchievementById(achievementId: Long): Achievement? {
        return withContext(Dispatchers.IO) {
            achievementDao.getAchievementById(achievementId)
        }
    }

    suspend fun updateAchievement(achievement: Achievement): Long {
        withContext(Dispatchers.IO) {
            achievementDao.updateAchievement(achievement)
        }
        return achievement.id
    }

    fun getAchievementsForChild(childId: Long): Flow<List<Achievement>> {
        return achievementDao.getAchievementsForChild(childId)
    }

    fun getChildrenRanking(): Flow<List<AchievementDao.ChildWithPoints>> {
        return achievementDao.getChildrenRanking()
    }

    suspend fun insertAchievement(achievement: Achievement): Long {
        return withContext(Dispatchers.IO) {
            achievementDao.insertAchievement(achievement)
        }
    }

    suspend fun deleteAchievement(achievement: Achievement) {
        withContext(Dispatchers.IO) {
            achievementDao.deleteAchievement(achievement)
        }
    }
}