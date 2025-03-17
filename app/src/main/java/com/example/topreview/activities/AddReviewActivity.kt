package com.example.topreview.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.topreview.R
import com.example.topreview.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddReviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var uploadButton: Button
    private lateinit var progressBar: ProgressBar
    private var imageUri: Uri? = null
    private val reviewViewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("AddReviewActivityLogs", "Activity started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        imageView = findViewById(R.id.imageView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        uploadButton = findViewById(R.id.uploadButton)
        progressBar = findViewById(R.id.progressBar)

        imageView.setOnClickListener { selectImageFromGallery() }
        uploadButton.setOnClickListener { uploadReview() }

        reviewViewModel.uploadStatus.observe(this, Observer { status ->
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
            Log.d("AddReviewActivityLogs", "Upload status: $status")
            if (status == "Review uploaded!") {
                progressBar.visibility = View.GONE
                Log.d("AddReviewActivityLogs", "Review uploaded successfully!")

                val resultIntent = Intent()
                // Optionally, you can pass back data, such as review ID or any other details
                resultIntent.putExtra("uploadStatus", "Review uploaded!")

                // Ensuring a small delay to simulate asynchronous operation if necessary
                lifecycleScope.launch {
                    delay(500)
                    Log.d("AddReviewActivityLogs", "Sending result back to HomeActivity")
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        })

        reviewViewModel.loading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadReview() {
        val description = descriptionEditText.text.toString().trim()
        if (imageUri != null && description.isNotEmpty()) {
            Log.d("AddReviewActivityLogs", "Uploading review: $description")
            reviewViewModel.uploadReview(imageUri!!, description)
        } else {
            if (imageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}
