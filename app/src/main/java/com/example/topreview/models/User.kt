package com.example.topreview.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String = "",
    val name: String = "",
    val imageUrl: String = ""
)