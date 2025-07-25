package com.example.topreview.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.topreview.databinding.FragmentEditReviewBinding
import com.example.topreview.model.Review
import com.example.topreview.model.ReviewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class EditReviewFragment : Fragment() {

    private var _binding: FragmentEditReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var review: Review
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
            showToast("City selection error: ${status.statusMessage}")
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

        review = EditReviewFragmentArgs.fromBundle(requireArguments()).review

        binding.editTextDescription.setText(review.description)
        binding.ratingBar.rating = review.rating
        binding.editTextCity.setText(review.city)
        selectedCity = review.city

        if (review.imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(review.imageUrl)
                .into(binding.imageViewReview)
            binding.imageCardView.visibility = View.VISIBLE
        }

        binding.editTextCity.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            )
                .setTypeFilter(TypeFilter.CITIES)
                .setCountries(listOf("IL"))
                .build(requireContext())
            cityPickerLauncher.launch(intent)
        }

        binding.buttonChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonSave.setOnClickListener {
            onSaveClicked()
        }
    }

    private fun onSaveClicked() {
        val updatedDescription = binding.editTextDescription.text.toString().trim()
        val updatedRating = binding.ratingBar.rating
        val updatedTimestamp = System.currentTimeMillis()

        val city = selectedCity
        if (updatedDescription.isBlank() || city.isNullOrEmpty()) {
            showToast("Please fill all fields")
            return
        }

        val updatedReview = review.copy(
            description = updatedDescription,
            rating = updatedRating,
            city = city,
            timestamp = updatedTimestamp
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSave.isEnabled = false

        if (imageUri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            ReviewModel.shared.add(updatedReview, bitmap) {
                postSaveCleanup()
            }
        } else {
            ReviewModel.shared.add(updatedReview, null) {
                postSaveCleanup()
            }
        }
    }

    private fun postSaveCleanup() {
        binding.progressBar.visibility = View.GONE
        binding.buttonSave.isEnabled = true
        showToast("Review updated successfully!")
        findNavController().navigateUp()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
