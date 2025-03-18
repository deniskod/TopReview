package com.example.topreview.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.topreview.R
import com.example.topreview.models.Review
import de.hdodenhof.circleimageview.CircleImageView


class ReviewAdapter(private var reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // Create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    // Bind the review data to the view holder
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.descriptionTextView.text = review.description

        // Set the rating text in the format "X/5"
        holder.textViewRating.text = "${review.rating}/5"

        // Load image using Glide
        Glide.with(holder.imageView.context)
            .load(review.imageUrl)  // The image URL from Firebase Storage
            .into(holder.imageView)
    }

    // Return the number of reviews
    override fun getItemCount(): Int {
        return reviews.size
    }

    // Update the reviews list and notify the adapter
    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews // Update the list reference
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    // ViewHolder class
    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
        val imageView: CircleImageView = itemView.findViewById(R.id.imageViewReview)
    }
}

