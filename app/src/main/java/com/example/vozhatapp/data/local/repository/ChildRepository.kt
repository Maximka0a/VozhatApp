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
    fun getAllChildren(): Flow<List<Child>> = childDao.getAllChildren()


    fun getChildWithDetails(childId: Long): Flow<ChildWithDetails?> {
        return childWithDetailsDao.getChildWithDetails(childId)
    }

    fun getChildById(id: Long): Flow<Child> = childDao.getChildById(id)

    fun getChildrenBySquad(squadName: String): Flow<List<Child>> =
        childDao.getChildrenBySquad(squadName)

    fun getAllSquadNames(): Flow<List<String>> = childDao.getAllSquadNames()

    fun searchChildren(query: String): Flow<List<Child>> =
        childDao.searchChildren(query)


    suspend fun insertChild(child: Child): Long = childDao.insertChild(child)

    suspend fun updateChild(child: Child) = childDao.updateChild(child)

    suspend fun deleteChild(child: Child) = childDao.deleteChild(child)
    suspend fun populateSampleData() {
        val sampleChildren = listOf(
            Child(
                name = "Иван",
                lastName = "Петров",
                age = 8,
                squadName = "Отряд А",
                medicalNotes = "Аллергия на орехи"
            ),
            Child(
                name = "Мария",
                lastName = "Иванова",
                age = 9,
                squadName = "Отряд А",
                parentPhone = "+7 (900) 123-45-67"
            ),
            Child(
                name = "Алексей",
                lastName = "Смирнов",
                age = 10,
                squadName = "Отряд Б"
            ),
            Child(
                name = "Екатерина",
                lastName = "Соколова",
                age = 7,
                squadName = "Отряд Б",
                medicalNotes = "Астма"
            ),
            Child(
                name = "Дмитрий",
                lastName = "Козлов",
                age = 9,
                squadName = "Отряд В",
                parentPhone = "+7 (900) 987-65-43",
                parentEmail = "kozlov@example.com"
            )
        )

        sampleChildren.forEach { childDao.insertChild(it) }
    }
}