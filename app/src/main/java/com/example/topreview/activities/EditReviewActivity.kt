package com.example.topreview.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.topreview.R
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.models.Review
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditReviewActivity : AppCompatActivity() {

    private lateinit var descriptionEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var imageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var changeImageButton: Button
    private lateinit var review: Review
    private lateinit var reviewRepository: ReviewRepository
    private var imageUri: Uri? = null // To store the selected image URI
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_review)

        // Initialize views
        descriptionEditText = findViewById(R.id.editTextDescription)
        ratingBar = findViewById(R.id.ratingBar)
        imageView = findViewById(R.id.imageViewReview)
        saveButton = findViewById(R.id.buttonSave)
        changeImageButton = findViewById(R.id.buttonChangeImage)
        progressBar = findViewById(R.id.progressBar)

        // Initialize repository
        val db = DatabaseProvider.getDatabase(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        // Get the review passed from the intent
        review = intent.getParcelableExtra("REVIEW") ?: return
        Log.d("nicelog", "review in editReview $review")

        // Set the existing review data to the views
        descriptionEditText.setText(review.description)
        ratingBar.rating = review.rating

        // Set the image if exists using Glide (for URL loading)
        if (review.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(review.imageUrl)
                .into(imageView)
        }

        // Handle image change button click
        changeImageButton.setOnClickListener {
            // Open the gallery to pick an image
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_REQUEST)
        }

        // Handle save button click
        saveButton.setOnClickListener {
            val updatedDescription = descriptionEditText.text.toString()
            val updatedRating = ratingBar.rating
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val currentTimestamp = System.currentTimeMillis()
            progressBar.visibility = View.VISIBLE
            // If an image was selected, upload the image and update the image URL
            if (imageUri != null) {
                FirebaseHelper.uploadImageToFirebaseStorage(imageUri!!,userId) { imageUrl ->
                    updateReview(updatedDescription, updatedRating, imageUrl,currentTimestamp)
                }
            } else {
                // If no new image selected, update the review with the existing image URL
                updateReview(updatedDescription, updatedRating, review.imageUrl,currentTimestamp)
            }
        }
    }

    // Update the review in the database
    private fun updateReview(description: String, rating: Float, imageUrl: String,timestamp: Long) {
        val updatedReview = review.copy(description = description, rating = rating, imageUrl = imageUrl, timestamp = timestamp)

        lifecycleScope.launch(Dispatchers.IO) {
            reviewRepository.updateReview(updatedReview)
        }

        Toast.makeText(this, "Review updated successfully!", Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
        finish()  // Close the activity
    }

    // Handle the result from the image picker activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_REQUEST) {
            data?.data?.let { uri ->
                imageUri = uri
                Log.d("ImageUri", "Picked Image URI: $uri")
                imageView.setImageURI(uri)  // Update the ImageView with the selected image
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_REQUEST = 1
    }
}
