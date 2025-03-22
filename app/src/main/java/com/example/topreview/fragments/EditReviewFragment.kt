package com.example.topreview.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.databinding.FragmentEditReviewBinding
import com.example.topreview.models.Review
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditReviewFragment : Fragment() {

    private var _binding: FragmentEditReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var review: Review
    private lateinit var reviewRepository: ReviewRepository
    private var imageUri: Uri? = null

    companion object {
        private const val IMAGE_PICK_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DatabaseProvider.getDatabase(requireContext())
        reviewRepository = ReviewRepository(db.reviewDao())

        // Get review from arguments (passed using SafeArgs or bundle)
        val args = EditReviewFragmentArgs.fromBundle(requireArguments())
        review = args.review
        Log.d("nicelog", "review in editReview $review")

        binding.editTextDescription.setText(review.description)
        binding.ratingBar.rating = review.rating

        if (review.imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(review.imageUrl)
                .into(binding.imageViewReview)
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_REQUEST)
        }

        binding.buttonSave.setOnClickListener {
            val updatedDescription = binding.editTextDescription.text.toString()
            val updatedRating = binding.ratingBar.rating
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val currentTimestamp = System.currentTimeMillis()
            binding.progressBar.visibility = View.VISIBLE

            if (imageUri != null) {
                FirebaseHelper.uploadImageToFirebaseStorage(imageUri!!, userId) { imageUrl ->
                    updateReview(updatedDescription, updatedRating, imageUrl, currentTimestamp)
                }
            } else {
                updateReview(updatedDescription, updatedRating, review.imageUrl, currentTimestamp)
            }
        }
    }

    private fun updateReview(description: String, rating: Float, imageUrl: String, timestamp: Long) {
        val updatedReview = review.copy(
            description = description,
            rating = rating,
            imageUrl = imageUrl,
            timestamp = timestamp
        )

        lifecycleScope.launch(Dispatchers.IO) {
            reviewRepository.updateReview(updatedReview)
        }

        Toast.makeText(requireContext(), "Review updated successfully!", Toast.LENGTH_SHORT).show()
        binding.progressBar.visibility = View.GONE
        findNavController().navigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_REQUEST) {
            data?.data?.let { uri ->
                imageUri = uri
                Log.d("ImageUri", "Picked Image URI: $uri")
                binding.imageViewReview.setImageURI(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
