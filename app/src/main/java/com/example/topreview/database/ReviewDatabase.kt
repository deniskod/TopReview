package com.example.topreview.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.topreview.dao.ReviewDao
import com.example.topreview.dao.UserDao
import com.example.topreview.models.Review
import com.example.topreview.models.User

@Database(entities = [Review::class,User::class], version = 2)  // Ensure this version matches the one in your migration
abstract class ReviewDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
    abstract fun userDao(): UserDao
}

