package com.example.topreview.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.topreview.R
import com.example.topreview.databinding.FragmentAddReviewBinding
import com.example.topreview.model.Review
import com.example.topreview.model.ReviewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class AddReviewFragment : Fragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!

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
            showToast("City selection error: ${status.statusMessage}")
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddReviewBinding.inflate(inflater, container, false)
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

        binding.progressBar.visibility = View.GONE
        binding.buttonSubmitReview.isEnabled = false

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editTextCity.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
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
            submitReview()
        }
    }

    private fun submitReview() {
        if (!validateFields()) return

        val description = binding.editTextDescription.text.toString().trim()
        val rating = binding.ratingBar.rating
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val review = Review(
            id = UUID.randomUUID().toString(),
            description = description,
            rating = rating,
            city = selectedCity!!,
            imageUrl = "",
            userId = userId,
        )

        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSubmitReview.isEnabled = false

        ReviewModel.shared.add(review, bitmap) {
            binding.progressBar.visibility = View.GONE
            findNavController().popBackStack()
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        getTextInputLayout(binding.editTextDescription)?.error = null
        getTextInputLayout(binding.editTextCity)?.error = null

        if (binding.editTextDescription.text.isNullOrBlank()) {
            getTextInputLayout(binding.editTextDescription)?.error = "Description is required"
            isValid = false
        }

        if (selectedCity.isNullOrBlank()) {
            getTextInputLayout(binding.editTextCity)?.error = "Please select a city"
            isValid = false
        }

        if (binding.ratingBar.rating == 0f) {
            showToast("Please select a rating!")
            isValid = false
        }

        if (imageUri == null) {
            showToast("Please select an image!")
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
        val enabled = description.isNotBlank() && rating > 0 && imageUri != null && !selectedCity.isNullOrBlank()

        binding.buttonSubmitReview.isEnabled = enabled
        val colorRes = if (enabled) R.color.button_enabled_bg else R.color.button_disabled_bg
        binding.buttonSubmitReview.setBackgroundColor(resources.getColor(colorRes, requireContext().theme))
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
