package com.example.topreview.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.topreview.databinding.FragmentEditReviewBinding
import com.example.topreview.models.Review
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.utils.FirebaseHelper
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditReviewFragment : Fragment() {

    private var _binding: FragmentEditReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var review: Review
    private lateinit var reviewRepository: ReviewRepository
    private var imageUri: Uri? = null
    private var selectedCity: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            imageUri = result.data!!.data
            binding.imageViewReview.setImageURI(imageUri)
            binding.imageCardView.visibility = View.VISIBLE
        }
    }

    private val cityPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            selectedCity = place.name
            binding.editTextCity.setText(selectedCity)
        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR && result.data != null) {
            val status = Autocomplete.getStatusFromIntent(result.data!!)
            Toast.makeText(requireContext(), "City selection error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, android.content.pm.PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized() && apiKey != null) {
            Places.initialize(requireContext().applicationContext, apiKey)
        }

        reviewRepository = ReviewRepository()

        val args = EditReviewFragmentArgs.fromBundle(requireArguments())
        review = args.review

        binding.editTextDescription.setText(review.description)
        binding.ratingBar.rating = review.rating
        binding.editTextCity.setText(review.city ?: "")
        selectedCity = review.city

        if (review.imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(review.imageUrl)
                .into(binding.imageViewReview)
            binding.imageCardView.visibility = View.VISIBLE
        } else {
            binding.imageCardView.visibility = View.GONE
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editTextCity.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                fields
            )
                .setTypeFilter(TypeFilter.CITIES)
                .setCountries(listOf("IL"))
                .build(requireContext())

            cityPickerLauncher.launch(intent)
        }

        binding.buttonChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.buttonSave.setOnClickListener {
            val updatedDescription = binding.editTextDescription.text.toString().trim()
            val updatedRating = binding.ratingBar.rating
            val updatedTimestamp = System.currentTimeMillis()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (updatedDescription.isBlank() || selectedCity.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.buttonSave.isEnabled = false

            if (imageUri != null) {
                FirebaseHelper.uploadImageToFirebaseStorage(imageUri!!, userId) { imageUrl ->
                    updateReview(updatedDescription, updatedRating, imageUrl, selectedCity!!, updatedTimestamp)
                }
            } else {
                updateReview(updatedDescription, updatedRating, review.imageUrl, selectedCity!!, updatedTimestamp)
            }
        }
    }

    private fun updateReview(
        description: String,
        rating: Float,
        imageUrl: String,
        city: String,
        timestamp: Long
    ) {
        val updatedReview = review.copy(
            description = description,
            rating = rating,
            city = city,
            imageUrl = imageUrl,
            timestamp = timestamp
        )

        lifecycleScope.launch(Dispatchers.IO) {
            reviewRepository.updateReview(updatedReview)
            launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Review updated successfully!", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.buttonSave.isEnabled = true
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
