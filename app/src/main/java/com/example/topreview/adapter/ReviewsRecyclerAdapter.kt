package com.example.topreview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.topreview.databinding.ItemReviewBinding
import com.example.topreview.fragments.OnReviewActionListener
import com.example.topreview.model.Review
import com.example.topreview.model.User

class ReviewsRecyclerAdapter(
    private var reviews: List<Review>,
    private val currentUserId: String,
    private val userMap: Map<String, User>,
    private val listener: OnReviewActionListener?
) : RecyclerView.Adapter<ReviewViewHolder>() {

    fun update(newReviews: List<Review>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = reviews.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReviewBinding.inflate(inflater, parent, false)
        return ReviewViewHolder(binding, currentUserId, listener)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        val user = userMap[review.userId]
        holder.bind(review, user)
    }
}
