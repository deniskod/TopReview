package com.example.topreview.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.topreview.R
import com.example.topreview.activities.EditReviewActivity
import com.example.topreview.models.Review
import de.hdodenhof.circleimageview.CircleImageView

class ReviewAdapter(private var reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }


    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.descriptionTextView.text = review.description

        holder.textViewRating.text = "${review.rating}/5"

        Glide.with(holder.imageView.context)
            .load(review.imageUrl)
            .into(holder.imageView)

        holder.imageEditReview.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditReviewActivity::class.java)
            Log.d("nicelog","review to editReview $review ")
            intent.putExtra("REVIEW", review)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
        val imageView: CircleImageView = itemView.findViewById(R.id.imageViewReview)

        val imageEditReview: ImageView = itemView.findViewById(R.id.imageEditReview)
    }
}
