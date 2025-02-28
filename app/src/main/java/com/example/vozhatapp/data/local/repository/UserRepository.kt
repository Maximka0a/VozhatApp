package com.example.vozhatapp.data.repository

import com.example.vozhatapp.data.local.dao.UserDao
import com.example.vozhatapp.data.local.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getUserById(userId: Long): Flow<User?> {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByEmail(email)
        }
    }

    suspend fun insertUser(user: User): Long {
        return withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.updateUser(user)
        }
    }

    suspend fun updateLastLogin(userId: Long) {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserByIdSync(userId)
            user?.let {
                userDao.updateUser(it.copy(lastLogin = System.currentTimeMillis()))
            }
        }
    }

    suspend fun deleteUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.deleteUser(user)
        }
    }

    suspend fun authenticate(email: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email)
            // В реальном приложении здесь должна быть безопасная проверка пароля
            if (user != null && user.password == password) {
                user
            } else {
                null
            }
        }
    }
}