package com.example.vozhatapp.data.repository

import com.example.vozhatapp.data.local.dao.GameDao
import com.example.vozhatapp.data.local.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao
) {
    val allGames: Flow<List<Game>> = gameDao.getAllGames()

    fun getGamesByCategory(category: String): Flow<List<Game>> {
        return gameDao.getGamesByCategory(category)
    }

    fun getGameById(gameId: Long): Flow<Game?> {
        return gameDao.getGameById(gameId)
    }

    fun searchGames(query: String): Flow<List<Game>> {
        return gameDao.searchGames(query)
    }

    suspend fun insertGame(game: Game): Long {
        return withContext(Dispatchers.IO) {
            gameDao.insertGame(game)
        }
    }

    suspend fun updateGame(game: Game) {
        withContext(Dispatchers.IO) {
            gameDao.updateGame(game)
        }
    }

    suspend fun deleteGame(game: Game) {
        withContext(Dispatchers.IO) {
            gameDao.deleteGame(game)
        }
    }
}