package com.example.topreview.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.topreview.model.Review

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReview(review: Review)

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserReviews(userId: String): LiveData<List<Review>>

    @Delete
    fun delete(review: Review)

    @Query("DELETE FROM reviews")
    fun clearAll()
}