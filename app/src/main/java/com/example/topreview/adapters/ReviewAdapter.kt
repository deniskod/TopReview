package com.example.topreview.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.topreview.R
import com.example.topreview.models.Review
import com.example.topreview.models.User
import de.hdodenhof.circleimageview.CircleImageView

class ReviewAdapter(
    private var reviews: List<Review>,
    private val currentUserId: String,
    private val userMap: Map<String, User>,
    private val onEditClicked: (Review) -> Unit,
    private val onDeleteClicked: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.descriptionTextView.text = review.description
        holder.textViewRating.text = "${review.rating}/5"
        holder.textViewCity.text = review.city

        val user = userMap[review.userId]
        holder.textViewUserName.text = user?.let { "${it.firstName} ${it.lastName}" } ?: "By: Unknown"

        Glide.with(holder.imageView.context)
            .load(review.imageUrl)
            .into(holder.imageView)

        if (review.userId == currentUserId) {
            holder.imageEditReview.visibility = View.VISIBLE
            holder.imageDeleteReview.visibility = View.VISIBLE

            holder.imageEditReview.setOnClickListener {
                onEditClicked(review)
            }

            holder.imageDeleteReview.setOnClickListener {
                onDeleteClicked(review)
            }
        } else {
            holder.imageEditReview.visibility = View.GONE
            holder.imageDeleteReview.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
        val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
        val textViewCity: TextView = itemView.findViewById(R.id.textViewCity)
        val imageView: CircleImageView = itemView.findViewById(R.id.imageViewReview)
        val imageEditReview: ImageView = itemView.findViewById(R.id.imageEditReview)
        val imageDeleteReview: ImageView = itemView.findViewById(R.id.imageDeleteReview)
    }

}