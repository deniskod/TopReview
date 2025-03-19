package com.example.topreview.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.topreview.R
import com.example.topreview.adapters.ReviewAdapter
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.models.Review
import com.example.topreview.repository.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var btnMyReviews: Button
    private var showAllReviews = true

    private var popupMenu: androidx.appcompat.view.menu.MenuPopupHelper? = null  // For handling potential PopupMenu leaks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerViewReviews)
        btnMyReviews = findViewById(R.id.btnMyReviews)

        // Initialize the database and repository
        val db = DatabaseProvider.getDatabase(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(emptyList())  // Start with an empty list
        recyclerView.adapter = reviewAdapter

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Get current user ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""  // If user is not logged in, empty string

        // Observe all reviews or user reviews depending on the toggle
        observeReviews(userId)

        // Handle "My Reviews" button click
        btnMyReviews.setOnClickListener {
            showAllReviews = !showAllReviews
            if (showAllReviews) {
                btnMyReviews.text = "My Reviews"  // Change button text to show "My Reviews"
                observeReviews(userId)  // Fetch all reviews
            } else {
                btnMyReviews.text = "Show All Reviews"  // Change button text to show "All Reviews"
                observeReviews(userId)  // Fetch user-specific reviews
            }
        }

        // Add review button
        val btnAddReview = findViewById<Button>(R.id.btnAddReview)
        btnAddReview.setOnClickListener {
            val intent = Intent(this, AddReviewActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit_profile -> {
                // Navigate to Edit Profile screen
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_logout -> {
                // Perform logout
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java)) // Redirect to Login
                finish() // Close HomeActivity
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeReviews(userId: String) {
        // Observe reviews based on the button state
        if (showAllReviews) {
            // Get all reviews from repository
            reviewRepository.getAllReviews().observe(this, Observer { reviews ->
                Log.d("HALOGS", "all reviews ${reviews.size}")
                if (reviews.isNotEmpty()) {
                    reviewAdapter.updateReviews(reviews)
                } else {
                    Toast.makeText(this@HomeActivity, "No reviews available", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Get user reviews only
            GlobalScope.launch(Dispatchers.Main) {
                val userReviews = reviewRepository.getUserReviews(userId)
                Log.d("HALOGS", "user reviews ${userReviews.size}")
                if (userReviews.isNotEmpty()) {
                    reviewAdapter.updateReviews(userReviews)
                } else {
                    Toast.makeText(this@HomeActivity, "You have no reviews", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // Dismiss any open PopupMenu or similar windows when the activity is paused
        popupMenu?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Dismiss any open PopupMenu or similar windows when the activity is destroyed
        popupMenu?.dismiss()
    }
}
