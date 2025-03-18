package com.example.topreview.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.topreview.models.Review

@Dao
interface ReviewDao {

    @Insert
    suspend fun insertReview(review: Review)

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getUserReviews(userId: String): List<Review>

    @Delete
    suspend fun deleteReview(review: Review)

    @Update
    suspend fun updateReview(review: Review)
}