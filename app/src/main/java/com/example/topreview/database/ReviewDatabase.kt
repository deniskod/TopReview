package com.example.topreview.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.topreview.dao.ReviewDao
import com.example.topreview.models.Review

@Database(entities = [Review::class], version = 1)
abstract class ReviewDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
}