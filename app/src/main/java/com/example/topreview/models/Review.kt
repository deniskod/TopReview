package com.example.topreview.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey var id: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val city: String = "",
    val imageUrl: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
