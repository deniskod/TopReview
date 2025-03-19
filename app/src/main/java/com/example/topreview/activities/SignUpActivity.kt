package com.example.topreview.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.topreview.databinding.ActivitySignUpBinding
import com.example.topreview.models.User
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.repository.UserRepository
import com.example.topreview.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseProvider.getDatabase(applicationContext)
        userRepository = UserRepository(db.userDao())

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val username = binding.usernameEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Sign up with Firebase Authentication
                authViewModel.signUp(email, password) { success, message ->
                    if (success) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val userId = user?.uid ?: return@signUp

                        val userData = User(
                            uid = userId,
                            username = username,
                            email = email
                        )

                        // Insert the user into Room database
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                userRepository.insertUser(userData)
                                runOnUiThread {
                                    Toast.makeText(this@SignUpActivity, "User signed up successfully", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                                    finish()  // Close SignUpActivity
                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    Toast.makeText(this@SignUpActivity, "Failed to store user data locally: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
