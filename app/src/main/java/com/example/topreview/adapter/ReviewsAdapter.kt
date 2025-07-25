package com.example.topreview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.topreview.R
import com.example.topreview.model.Review
import com.example.topreview.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ReviewsAdapter(
    private val reviews: List<Review>,
    private val currentUserId: String,
    private val userMap: Map<String, User>,
    private val onEditClicked: (Review) -> Unit,
    private val onDeleteClicked: (Review) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = reviews.size

    override fun getItem(position: Int): Any = reviews[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: View.inflate(parent?.context, R.layout.item_review, null)

        val review = reviews[position]

        val descriptionTextView: TextView = view.findViewById(R.id.textViewDescription)
        val textViewRating: TextView = view.findViewById(R.id.textViewRating)
        val textViewUserName: TextView = view.findViewById(R.id.textViewUserName)
        val textViewCity: TextView = view.findViewById(R.id.textViewCity)
        val imageView: CircleImageView = view.findViewById(R.id.imageViewReview)
        val imageEditReview: ImageView = view.findViewById(R.id.imageEditReview)
        val imageDeleteReview: ImageView = view.findViewById(R.id.imageDeleteReview)

        descriptionTextView.text = review.description
        textViewRating.text = "${review.rating}/5"
        textViewCity.text = review.city

        val user = userMap[review.userId]
        textViewUserName.text = user?.name ?: "By: Unknown"

        Picasso.get()
            .load(review.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(imageView)

        if (review.userId == currentUserId) {
            imageEditReview.visibility = View.VISIBLE
            imageDeleteReview.visibility = View.VISIBLE

            imageEditReview.setOnClickListener { onEditClicked(review) }
            imageDeleteReview.setOnClickListener { onDeleteClicked(review) }
        } else {
            imageEditReview.visibility = View.GONE
            imageDeleteReview.visibility = View.GONE
        }

        return view
    }
}
