package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Child
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ChildDao {
    @Query("SELECT * FROM children ORDER BY lastName, name")
    fun getAllChildren(): Flow<List<Child>>

    @Query("SELECT * FROM children WHERE id = :childId")
    fun getChildById(childId: Long): Flow<Child>

    @Query("SELECT * FROM children WHERE squadName = :squadName ORDER BY lastName, name")
    fun getChildrenBySquad(squadName: String): Flow<List<Child>>

    @Query("SELECT DISTINCT squadName FROM children ORDER BY squadName")
    fun getAllSquadNames(): Flow<List<String>>

    @Query("SELECT * FROM children WHERE name LIKE '%' || :searchQuery || '%' OR lastName LIKE '%' || :searchQuery || '%' ORDER BY lastName, name")
    fun searchChildren(searchQuery: String): Flow<List<Child>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: Child): Long

    @Update
    suspend fun updateChild(child: Child)

    @Delete
    suspend fun deleteChild(child: Child)
}