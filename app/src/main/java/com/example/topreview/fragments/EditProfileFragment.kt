package com.example.topreview.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.databinding.FragmentEditProfileBinding
import com.example.topreview.repository.UserRepository
import com.example.topreview.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userRepository: UserRepository
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            binding.imageViewProfile.setImageURI(selectedImageUri)
            binding.imageCardView.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val db = DatabaseProvider.getDatabase(requireContext())
        userRepository = UserRepository(db.userDao())

        loadUserData()

        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imagePickerLauncher.launch(intent)
        }

        binding.buttonSaveProfile.setOnClickListener {
            val newFirstName = binding.editTextFirstName.text.toString().trim()
            val newLastName = binding.editTextLastName.text.toString().trim()

            if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                FirebaseHelper.uploadImageToFirebaseStorage(selectedImageUri!!, userId) { imageUrl ->
                    if (imageUrl != null) {
                        saveUserProfile(newFirstName, newLastName, imageUrl)
                    } else {
                        Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                saveUserProfile(newFirstName, newLastName, currentImageUrl)
            }
        }
    }



    private fun loadUserData() {
        lifecycleScope.launch {
            val user = userRepository.getUserById(userId)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    binding.editTextFirstName.setText(user.name.split(' ')[0])
                    binding.editTextLastName.setText(user.name.split(' ')[1])
                    currentImageUrl = user.imageUrl

                    if (!currentImageUrl.isNullOrEmpty()) {
                        FirebaseHelper.loadImageIntoImageView(currentImageUrl!!, binding.imageViewProfile)
                        binding.imageCardView.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveUserProfile(firstName: String, lastName: String, imageUrl: String?) {
        lifecycleScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    name = "$firstName $lastName",
                    imageUrl = imageUrl ?: user.imageUrl
                )
                userRepository.updateUser(updatedUser)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
