package com.example.topreview.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.topreview.R
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.databinding.FragmentAddReviewBinding
import com.example.topreview.models.Review
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.utils.FirebaseHelper
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddReviewFragment : Fragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewRepository: ReviewRepository
    private var imageUri: Uri? = null
    private var selectedCity: String? = null

    private val cityPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            selectedCity = place.name
            binding.editTextCity.setText(selectedCity)
            checkSubmitButtonState()
        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(result.data!!)
            Toast.makeText(requireContext(), "City selection error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            imageUri = result.data?.data
            binding.imageViewSelected.setImageURI(imageUri)
            binding.imageCardView.visibility = View.VISIBLE
            checkSubmitButtonState()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Places if not yet initialized
        val apiKey = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, android.content.pm.PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized() && apiKey != null) {
            Places.initialize(requireContext().applicationContext, apiKey)
        }

        val db = DatabaseProvider.getDatabase(requireContext())
        reviewRepository = ReviewRepository(db.reviewDao())

        binding.progressBar.visibility = View.GONE
        binding.buttonSubmitReview.isEnabled = false

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

        binding.editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = checkSubmitButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            checkSubmitButtonState()
        }

        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.buttonSubmitReview.setOnClickListener {
            if (!validateFields()) return@setOnClickListener

            val description = binding.editTextDescription.text.toString()
            val rating = binding.ratingBar.rating
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            binding.progressBar.visibility = View.VISIBLE
            binding.buttonSubmitReview.isEnabled = false

            imageUri?.let { uri ->
                FirebaseHelper.uploadImageToFirebaseStorage(uri, userId) { imageUrl ->

                    val review = Review(
                        description = description,
                        rating = rating,
                        city = selectedCity!!,
                        imageUrl = imageUrl,
                        userId = userId
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        reviewRepository.insertReview(review)
                        requireActivity().runOnUiThread {
                            binding.progressBar.visibility = View.GONE
                            binding.buttonSubmitReview.isEnabled = true
                            Toast.makeText(requireContext(), "Review added successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            } ?: run {
                Toast.makeText(requireContext(), "Image not selected", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.buttonSubmitReview.isEnabled = true
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        getTextInputLayout(binding.editTextDescription)?.error = null
        getTextInputLayout(binding.editTextCity)?.error = null

        val description = binding.editTextDescription.text.toString()
        if (description.isEmpty()) {
            getTextInputLayout(binding.editTextDescription)?.error = "Description is required"
            isValid = false
        }

        if (selectedCity.isNullOrEmpty()) {
            getTextInputLayout(binding.editTextCity)?.error = "Please select a city"
            isValid = false
        }

        if (binding.ratingBar.rating == 0f) {
            Toast.makeText(requireContext(), "Please select a rating!", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (imageUri == null) {
            Toast.makeText(requireContext(), "Please select an image!", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun getTextInputLayout(view: View): TextInputLayout? {
        return view.parent as? TextInputLayout
    }

    private fun checkSubmitButtonState() {
        val description = binding.editTextDescription.text.toString()
        val rating = binding.ratingBar.rating
        val isEnabled = description.isNotEmpty() && rating > 0 &&
                imageUri != null && !selectedCity.isNullOrEmpty()

        binding.buttonSubmitReview.isEnabled = isEnabled

        val colorResId = if (isEnabled) {
            R.color.button_enabled_bg
        } else {
            R.color.button_disabled_bg
        }

        binding.buttonSubmitReview.setBackgroundColor(
            resources.getColor(colorResId, requireContext().theme)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
