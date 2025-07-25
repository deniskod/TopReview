package com.example.topreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.topreview.model.Review
import com.example.topreview.model.ReviewModel

class ReviewsListViewModel : ViewModel() {

    var reviews: LiveData<List<Review>> = ReviewModel.shared.reviews

    fun refreshAllReviews() {
        ReviewModel.shared.refreshAllReviews()
    }
}