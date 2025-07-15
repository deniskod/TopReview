package com.example.topreview.repository

import android.util.Log
import com.example.topreview.models.Review
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val db = Firebase.firestore.collection("reviews")

    suspend fun getAllReviews(): List<Review> {
        return try {
            val snapshot = db.orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            val reviews = mutableListOf<Review>()
            for (document in snapshot.documents) {
                try {
                    val review = document.toObject(Review::class.java)
                    if (review != null) {
                        review.id = document.id
                        reviews.add(review)
                    }
                } catch (e: Exception) {
                    Log.e("getAllReviews()", "Failed to parse document: ${document.id}", e)
                }
            }
            reviews
        } catch (e: Exception) {
            Log.e("getAllReviews()", "Error fetching reviews", e)
            emptyList()
        }
    }

    fun insertReview(review: Review) {
        val docRef = db.document()
        val reviewWithId = review.copy(id = docRef.id)

        docRef.set(reviewWithId)
            .addOnSuccessListener {
                Log.d("insertReview()", "DocumentSnapshot successfully written with ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.w("insertReview()", "Error writing document", e)
            }
    }

    suspend fun getUserReviews(userId: String): List<Review> {
        return try {
            val snapshot = db.whereEqualTo("userId",userId).get().await()
            val reviews = mutableListOf<Review>()
            for (document in snapshot.documents) {
                try {
                    val review = document.toObject(Review::class.java)
                    if (review != null) {
                        reviews.add(review)
                    }
                } catch (e: Exception) {
                    Log.e("getUserReviews()", "Failed to parse document: ${document.id}", e)
                }
            }
            reviews
        } catch (e: Exception) {
            Log.e("getUserReviews()", "Error fetching reviews", e)
            emptyList()
        }
    }

    suspend fun deleteReview(review: Review) {
        try {
            db.document(review.id).delete().await()
            Log.d("deleteReview()", "Successfully deleted review with ID: ${review.id}")
        } catch (e: Exception) {
            Log.e("deleteReview()", "Failed to delete review with ID: ${review.id}", e)
        }
    }

    suspend fun updateReview(review: Review) {
        try {
            db.document(review.id).set(review).await()
            Log.d("updateReview()", "Successfully updated review with ID: ${review.id}")
        } catch (e: Exception) {
            Log.e("updateReview()", "Failed to update review with ID: ${review.id}", e)
        }
    }
}

