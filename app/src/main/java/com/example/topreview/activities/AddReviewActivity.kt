package com.example.topreview.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.topreview.R
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.models.Review
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.utils.FirebaseHelper
import com.example.topreview.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddReviewActivity : AppCompatActivity() {
    private lateinit var descriptionEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var selectImageButton: ImageButton
    private lateinit var submitButton: Button
    private lateinit var imageViewSelected: ImageView
    private lateinit var imageUri: Uri
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var imageCardView: FrameLayout
    private lateinit var backButton : ImageButton
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        descriptionEditText = findViewById(R.id.editTextDescription)
        ratingBar = findViewById(R.id.ratingBar)
        selectImageButton = findViewById(R.id.buttonSelectImage)
        submitButton = findViewById(R.id.buttonSubmitReview)
        imageViewSelected = findViewById(R.id.imageViewSelected)
        imageCardView = findViewById(R.id.imageCardView)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.buttonBack)

        val db = DatabaseProvider.getDatabase(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        submitButton.isEnabled = false

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkSubmitButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            checkSubmitButtonState()
        }

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }

        submitButton.setOnClickListener {
            val description = descriptionEditText.text.toString()
            val rating = ratingBar.rating
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rating == 0f) {
                Toast.makeText(this, "Please select a rating!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!::imageUri.isInitialized) {
                Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            FirebaseHelper.uploadImageToFirebaseStorage(imageUri, userId) { imageUrl ->
                val review = Review(
                    description = description,
                    rating = rating,
                    imageUrl = imageUrl,
                    userId = userId
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    reviewRepository.insertReview(review)
                }
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Review added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun checkSubmitButtonState() {
        val description = descriptionEditText.text.toString()
        val rating = ratingBar.rating
        submitButton.isEnabled = description.isNotEmpty() && rating > 0 && ::imageUri.isInitialized
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_REQUEST_CODE) {
            imageUri = data?.data!!

            imageViewSelected.setImageURI(imageUri)
            imageViewSelected.visibility = View.VISIBLE

            imageCardView.visibility = View.VISIBLE
            imageCardView.requestLayout()
            findViewById<FrameLayout>(R.id.imageCardView).visibility = View.VISIBLE
            checkSubmitButtonState()
        }
    }

    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 100
    }
}




