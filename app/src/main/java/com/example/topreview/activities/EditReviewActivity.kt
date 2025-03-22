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
import android.widget.ImageButton
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
    private lateinit var changeImageButton: ImageButton
    private lateinit var review: Review
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var buttonBack: ImageButton
    private var imageUri: Uri? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_review)

        descriptionEditText = findViewById(R.id.editTextDescription)
        ratingBar = findViewById(R.id.ratingBar)
        imageView = findViewById(R.id.imageViewReview)
        saveButton = findViewById(R.id.buttonSave)
        changeImageButton = findViewById(R.id.buttonChangeImage)
        progressBar = findViewById(R.id.progressBar)
        buttonBack = findViewById(R.id.buttonBack)

        buttonBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        val db = DatabaseProvider.getDatabase(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        review = intent.getParcelableExtra("REVIEW") ?: return
        Log.d("nicelog", "review in editReview $review")

        descriptionEditText.setText(review.description)
        ratingBar.rating = review.rating

        if (review.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(review.imageUrl)
                .into(imageView)
        }

        changeImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_REQUEST)
        }

        saveButton.setOnClickListener {
            val updatedDescription = descriptionEditText.text.toString()
            val updatedRating = ratingBar.rating
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val currentTimestamp = System.currentTimeMillis()
            progressBar.visibility = View.VISIBLE
            if (imageUri != null) {
                FirebaseHelper.uploadImageToFirebaseStorage(imageUri!!,userId) { imageUrl ->
                    updateReview(updatedDescription, updatedRating, imageUrl,currentTimestamp)
                }
            } else {
                updateReview(updatedDescription, updatedRating, review.imageUrl,currentTimestamp)
            }
        }
    }

    private fun updateReview(description: String, rating: Float, imageUrl: String,timestamp: Long) {
        val updatedReview = review.copy(description = description, rating = rating, imageUrl = imageUrl, timestamp = timestamp)

        lifecycleScope.launch(Dispatchers.IO) {
            reviewRepository.updateReview(updatedReview)
        }

        Toast.makeText(this, "Review updated successfully!", Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
        finish()
    }

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
