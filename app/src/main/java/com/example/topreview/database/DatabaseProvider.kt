package com.example.topreview.database

import androidx.room.Room
import android.content.Context

object DatabaseProvider {
    private var instance: ReviewDatabase? = null

    fun getDatabase(context: Context): ReviewDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                ReviewDatabase::class.java,
                "review_database"
            ).fallbackToDestructiveMigration().build()
        }
        return instance!!
    }
}
