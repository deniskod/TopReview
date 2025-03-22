package com.example.topreview.repository

import com.example.topreview.dao.UserDao
import com.example.topreview.models.User

class UserRepository(private val userDao: UserDao) {

    // Insert a user into the database
    suspend fun insertUser(user: User): Long {
        return userDao.insert(user)
    }

    // Get a user by UID
    suspend fun getUserById(uid: String): User? {
        return userDao.getUserById(uid)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user) // You need to implement this in your DAO
    }
}