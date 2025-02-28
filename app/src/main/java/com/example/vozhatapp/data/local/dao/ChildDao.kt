package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Child
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ChildDao {
    @Query("SELECT * FROM children ORDER BY name ASC")
    fun getAllChildren(): Flow<List<Child>>

    @Query("SELECT * FROM children WHERE squadName = :squadName ORDER BY name ASC")
    fun getChildrenBySquad(squadName: String): Flow<List<Child>>

    @Query("SELECT * FROM children WHERE id = :childId")
    fun getChildById(childId: Long): Flow<Child?>

    @Query("SELECT * FROM children WHERE name LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'")
    fun searchChildren(query: String): Flow<List<Child>>

    @Insert
    suspend fun insertChild(child: Child): Long

    @Update
    suspend fun updateChild(child: Child)

    @Delete
    suspend fun deleteChild(child: Child)
}