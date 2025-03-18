package com.example.topreview.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val rating: Float,
    val imageUrl: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

