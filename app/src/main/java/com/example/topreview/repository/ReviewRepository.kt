package com.example.topreview.repository

import androidx.lifecycle.LiveData
import com.example.topreview.dao.ReviewDao
import com.example.topreview.models.Review

class ReviewRepository(private val reviewDao: ReviewDao) {

    fun getAllReviews(): LiveData<List<Review>> {
        return reviewDao.getAllReviews()  // Room returns LiveData directly
    }

    suspend fun insertReview(review: Review) {
        reviewDao.insertReview(review)
    }

    suspend fun getUserReviews(userId: String): List<Review> {
        return reviewDao.getUserReviews(userId)
    }

    suspend fun deleteReview(review: Review) {
        reviewDao.deleteReview(review)
    }

    suspend fun updateReview(review: Review) {
        reviewDao.updateReview(review)
    }
}

