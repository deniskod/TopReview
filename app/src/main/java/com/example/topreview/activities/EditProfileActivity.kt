package com.example.topreview.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.topreview.databinding.ActivityEditProfileBinding
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.models.User
import com.example.topreview.repository.UserRepository
import com.example.topreview.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userRepository: UserRepository
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Modern way to launch the image picker and get result
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            binding.imageViewProfile.setImageURI(selectedImageUri)
            binding.imageCardView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseProvider.getDatabase(applicationContext)
        userRepository = UserRepository(db.userDao())

        // Load user data from Room
        lifecycleScope.launch {
            val user = userRepository.getUserById(userId)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    binding.editTextFirstName.setText(user.firstName)
                    binding.editTextLastName.setText(user.lastName)
                    currentImageUrl = user.imageUrl

                    if (!currentImageUrl.isNullOrEmpty()) {
                        FirebaseHelper.loadImageIntoImageView(currentImageUrl!!, binding.imageViewProfile)
                        binding.imageCardView.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@EditProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Open image picker
        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imagePickerLauncher.launch(intent)
        }

        // Save changes
        binding.buttonSaveProfile.setOnClickListener {
            val newFirstName = binding.editTextFirstName.text.toString().trim()
            val newLastName = binding.editTextLastName.text.toString().trim()

            if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                FirebaseHelper.uploadImageToFirebaseStorage(selectedImageUri!!, userId) { imageUrl ->
                    if (imageUrl != null) {
                        saveUserProfile(newFirstName, newLastName, imageUrl)
                    } else {
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                saveUserProfile(newFirstName, newLastName, currentImageUrl)
            }
        }
    }

    private fun saveUserProfile(firstName: String, lastName: String, imageUrl: String?) {
        lifecycleScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    imageUrl = imageUrl!!
                )
                userRepository.updateUser(updatedUser)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 101
    }
}
