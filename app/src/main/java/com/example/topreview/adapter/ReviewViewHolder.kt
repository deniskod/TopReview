package com.example.topreview.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.topreview.R
import com.example.topreview.databinding.ItemReviewBinding
import com.example.topreview.fragments.OnReviewActionListener
import com.example.topreview.model.Review
import com.example.topreview.model.User
import com.squareup.picasso.Picasso

class ReviewViewHolder(
    private val binding: ItemReviewBinding,
    private val currentUserId: String,
    private val listener: OnReviewActionListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(review: Review?, user: User?) {
        if (review == null) return

        binding.textViewDescription.text = review.description
        binding.textViewRating.text = "${review.rating}/5"
        binding.textViewCity.text = review.city
        binding.textViewUserName.text = user?.name ?: "By: Unknown"

        if (!review.imageUrl.isNullOrBlank()) {
            Picasso.get()
                .load(review.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.imageViewReview)
        }

        if (review.userId == currentUserId) {
            binding.imageEditReview.visibility = android.view.View.VISIBLE
            binding.imageDeleteReview.visibility = android.view.View.VISIBLE

            binding.imageEditReview.setOnClickListener { listener?.onEditClick(review) }
            binding.imageDeleteReview.setOnClickListener { listener?.onDeleteClick(review) }
        } else {
            binding.imageEditReview.visibility = android.view.View.GONE
            binding.imageDeleteReview.visibility = android.view.View.GONE
        }
    }
}
