package com.example.topreview.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.databinding.FragmentAddReviewBinding
import com.example.topreview.models.Review
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddReviewFragment : Fragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewRepository: ReviewRepository
    private var imageUri: Uri? = null

    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 100
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

        val db = DatabaseProvider.getDatabase(requireContext())
        reviewRepository = ReviewRepository(db.reviewDao())

        binding.progressBar.visibility = View.GONE
        binding.buttonSubmitReview.isEnabled = false

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkSubmitButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            checkSubmitButtonState()
        }

        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }

        binding.buttonSubmitReview.setOnClickListener {
            val description = binding.editTextDescription.text.toString()
            val rating = binding.ratingBar.rating
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a description!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rating == 0f) {
                Toast.makeText(requireContext(), "Please select a rating!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri == null) {
                Toast.makeText(requireContext(), "Please select an image!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            FirebaseHelper.uploadImageToFirebaseStorage(imageUri!!, userId) { imageUrl ->
                val review = Review(
                    description = description,
                    rating = rating,
                    imageUrl = imageUrl,
                    userId = userId
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    reviewRepository.insertReview(review)
                }

                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Review added successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun checkSubmitButtonState() {
        val description = binding.editTextDescription.text.toString()
        val rating = binding.ratingBar.rating
        binding.buttonSubmitReview.isEnabled = description.isNotEmpty() && rating > 0 && imageUri != null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_REQUEST_CODE) {
            imageUri = data?.data
            binding.imageViewSelected.setImageURI(imageUri)
            binding.imageCardView.visibility = View.VISIBLE
            checkSubmitButtonState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
