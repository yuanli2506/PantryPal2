package com.example.pantrypal.data.repository

import com.example.pantrypal.data.local.UserDao
import com.example.pantrypal.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    // Login user
    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    // Register user
    suspend fun register(user: User): Result<Long> {
        return try {
            if (userDao.isEmailExists(user.email)) {
                Result.failure(Exception("Email already exists"))
            } else {
                val id = userDao.insertUser(user)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user by email
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    // Get user by ID
    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    // Get user by ID as Flow
    fun getUserByIdFlow(id: Int): Flow<User?> {
        return userDao.getUserByIdFlow(id)
    }

    // Update user
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    // Check if email exists
    suspend fun isEmailExists(email: String): Boolean {
        return userDao.isEmailExists(email)
    }
}
