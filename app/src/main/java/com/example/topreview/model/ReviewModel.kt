package com.example.topreview.model

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.topreview.base.EmptyCallback
import com.example.topreview.model.dao.AppLocalDb
import com.example.topreview.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class ReviewModel private constructor() {

    enum class LoadingState {
        LOADING,
        LOADED
    }

    private val database: AppLocalDbRepository = AppLocalDb.database
    private var executor = Executors.newSingleThreadExecutor()
    val reviews: LiveData<List<Review>> = database.reviewDao().getAllReviews()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    private val firebaseModel = FirebaseModel()
    companion object {
        val shared = ReviewModel()
    }

    fun refreshAllReviews() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = Review.lastUpdated
        firebaseModel.getAllReviews() { reviews ->
            executor.execute {
                var currentTime = lastUpdated
                for (review in reviews) {
                    database.reviewDao().insertReview(review)
                    review.timestamp?.let {
                        if (currentTime < it) {
                            currentTime = it
                        }
                    }
                }

                Review.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun refreshAllUserReviews(userId: String) {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = Review.lastUpdated
        firebaseModel.getUserReviews(userId) { reviews ->
            executor.execute {
                var currentTime = lastUpdated
                for (review in reviews) {
                    database.reviewDao().insertReview(review)
                    review.timestamp?.let {
                        if (currentTime < it) {
                            currentTime = it
                        }
                    }
                }

                Review.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun add(review: Review, image: Bitmap?, callback: EmptyCallback) {
        firebaseModel.addReview(review) {
            image?.let {
                uploadTo(
                    image = image,
                    name = review.id,
                    callback = { uri ->
                        if (!uri.isNullOrBlank()) {
                            val st = review.copy(imageUrl = uri)
                            firebaseModel.addReview(st, callback)
                        } else {
                            callback()
                        }
                    },
                )
            } ?: callback()
        }
    }

    private fun uploadTo(image: Bitmap, name: String, callback: (String?) -> Unit) {
        uploadImageToFirebase(image, name, callback)
    }

    fun delete(review: Review, callback: EmptyCallback) {
        firebaseModel.deleteReview(review, callback)
    }

    private fun uploadImageToFirebase(
        image: Bitmap,
        name: String,
        callback: (String?) -> Unit
    ) {
        firebaseModel.uploadImage(image, name, callback)
    }
}