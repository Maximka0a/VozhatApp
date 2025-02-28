package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY title ASC")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE category = :category ORDER BY title ASC")
    fun getGamesByCategory(category: String): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameById(gameId: Long): Flow<Game?>

    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchGames(query: String): Flow<List<Game>>

    @Insert
    suspend fun insertGame(game: Game): Long

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)
}