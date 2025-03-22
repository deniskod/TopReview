package com.example.topreview.repository

import com.example.topreview.dao.UserDao
import com.example.topreview.models.User

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun getUserById(uid: String): User? {
        return userDao.getUserById(uid)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun getAll(): List<User> {
        return userDao.getAll()
    }
}