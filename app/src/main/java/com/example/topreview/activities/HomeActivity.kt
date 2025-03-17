package com.example.topreview.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.topreview.R
import com.example.topreview.adapters.ReviewAdapter
import com.example.topreview.models.Review
import com.example.topreview.viewmodel.ReviewViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class HomeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val reviewViewModel: ReviewViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private lateinit var filterButton: ToggleButton
    private lateinit var addReviewFab: FloatingActionButton
    private lateinit var logoutButton: MaterialButton

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("HomeActivityLogs", "Received RESULT_OK from AddReviewActivity")
            loadReviews()
        } else {
            Log.d("HomeActivityLogs", "Received other result: $result.resultCode")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.reviewsRecyclerView)
        filterButton = findViewById(R.id.filterButton)
        addReviewFab = findViewById(R.id.addReviewFab)
        logoutButton = findViewById(R.id.logoutButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(mutableListOf()) // Empty list initially
        recyclerView.adapter = adapter

        loadReviews() // Load all reviews initially

        filterButton.setOnCheckedChangeListener { _, isChecked ->
            loadReviews()  // Reload reviews based on filter
        }

        addReviewFab.setOnClickListener {
            val intent = Intent(this, AddReviewActivity::class.java)
            startForResult.launch(intent)  // Start activity and wait for result
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadReviews() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("HomeActivity", "User not authenticated.")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val reviewsCollection = db.collection("reviews")
        val query = if (filterButton.isChecked) {
            reviewsCollection.whereEqualTo("userId", userId)
        } else {
            reviewsCollection
        }

        query.get(Source.SERVER).addOnSuccessListener { documents ->
            val reviewsList = mutableListOf<Review>()
            for (doc in documents) {
                val review = doc.toObject(Review::class.java)
                reviewsList.add(review)
            }
            adapter.updateReviews(reviewsList)

        }.addOnFailureListener { serverException ->
            Log.e("HomeActivity", "Error fetching reviews from server: ", serverException)

            // Check if it's a permission issue
            if (serverException.message?.contains("Permission denied") == true) {
                Log.e("HomeActivity", "Permission denied. Check Firestore rules.")
                Toast.makeText(this, "Permission denied. Update Firestore rules.", Toast.LENGTH_LONG).show()
                return@addOnFailureListener
            }

            // If server fetch fails, try cache
            query.get(Source.CACHE).addOnSuccessListener { cachedDocuments ->
                val cachedReviewsList = mutableListOf<Review>()
                for (doc in cachedDocuments) {
                    val cachedReview = doc.toObject(Review::class.java)
                    cachedReviewsList.add(cachedReview)
                }
                adapter.updateReviews(cachedReviewsList)
            }.addOnFailureListener { cacheException ->
                Log.e("HomeActivity", "Error fetching reviews from cache: ", cacheException)
                Toast.makeText(this, "Failed to load reviews.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
