package com.example.topreview.activities

import AuthViewModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.topreview.databinding.ActivitySignUpBinding
import com.example.topreview.models.User
import com.example.topreview.repository.UserRepository
import com.example.topreview.utils.FirebaseHelper
import com.example.topreview.database.DatabaseProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var userRepository: UserRepository
    private var imageUri: Uri? = null

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseProvider.getDatabase(applicationContext)
        userRepository = UserRepository(db.userDao())

        // Image picker
        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()

            when {
                email.isEmpty() -> Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
                firstName.isEmpty() -> Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show()
                lastName.isEmpty() -> Toast.makeText(this, "Last name is required", Toast.LENGTH_SHORT).show()
                imageUri == null -> Toast.makeText(this, "Profile image is required", Toast.LENGTH_SHORT).show()
                else -> {
                    authViewModel.signUp(email, password) { success, message ->
                        if (success) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                            FirebaseHelper.uploadImageToFirebaseStorage(imageUri!!, userId) { imageUrl ->
                                saveUserAndFinish(userId, firstName, lastName, imageUrl)
                            }
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun saveUserAndFinish(
        userId: String,
        firstName: String,
        lastName: String,
        imageUrl: String,
    ) {
        val user = User(
            uid = userId,
            firstName = firstName,
            lastName = lastName,
            imageUrl = imageUrl,
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = userRepository.insertUser(user)
                if (result > 0) {
                    android.util.Log.d("SignUpActivity", "User successfully added to UserDao with rowId: $result")
                } else {
                    android.util.Log.e("SignUpActivity", "User insert returned rowId <= 0. Possibly failed.")
                }
            } catch (e: Exception) {
                android.util.Log.e("SignUpActivity", "Exception inserting user into UserDao: ${e.message}", e)
            }
        }

        Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            binding.imageViewSelected.setImageURI(imageUri)
            binding.imageCardView.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 100
    }
}
