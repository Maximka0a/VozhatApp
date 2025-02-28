package com.example.vozhatapp.data.local.repository

import com.example.vozhatapp.data.local.dao.ChildDao
import com.example.vozhatapp.data.local.dao.ChildWithDetailsDao
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.relation.ChildWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChildRepository(
    private val childDao: ChildDao,
    private val childWithDetailsDao: ChildWithDetailsDao
) {
    // Возвращает Flow с сущностями Child напрямую
    val allChildren: Flow<List<Child>> = childDao.getAllChildren()

    fun getChildrenBySquad(squadName: String): Flow<List<Child>> {
        return childDao.getChildrenBySquad(squadName)
    }

    fun getChildById(childId: Long): Flow<Child?> {
        return childDao.getChildById(childId)
    }

    fun getChildWithDetails(childId: Long): Flow<ChildWithDetails?> {
        return childWithDetailsDao.getChildWithDetails(childId)
    }

    fun searchChildren(query: String): Flow<List<Child>> {
        return childDao.searchChildren(query)
    }

    suspend fun insertChild(child: Child): Long {
        return withContext(Dispatchers.IO) {
            childDao.insertChild(child)
        }
    }

    suspend fun updateChild(child: Child) {
        withContext(Dispatchers.IO) {
            childDao.updateChild(child)
        }
    }

    suspend fun deleteChild(child: Child) {
        withContext(Dispatchers.IO) {
            childDao.deleteChild(child)
        }
    }
}