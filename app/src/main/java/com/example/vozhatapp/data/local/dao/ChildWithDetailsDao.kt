package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.relation.ChildWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildWithDetailsDao {
    @Transaction
    @Query("SELECT * FROM children WHERE id = :childId")
    fun getChildWithDetails(childId: Long): Flow<ChildWithDetails?>

    @Transaction
    @Query("SELECT * FROM children")
    fun getAllChildrenWithDetails(): Flow<List<ChildWithDetails>>
}