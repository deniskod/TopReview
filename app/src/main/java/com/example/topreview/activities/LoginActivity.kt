package com.example.topreview.activities

import AuthViewModel
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.topreview.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the binding object properly
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.signIn(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        // Navigate to HomeActivity after successful sign-in
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()  // Close LoginActivity
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle "Don't have an account? Sign up" button click
        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Handle "Forgot password?" button click
        binding.forgotPasswordButton.setOnClickListener {
            showForgotPasswordDialog(binding)
        }
    }

    private fun showForgotPasswordDialog(binding: ActivityLoginBinding) {
        // Here you can show a dialog or simply prompt the user for their email address
        val email = binding.emailEditText.text.toString()

        if (email.isNotEmpty()) {
            authViewModel.sendPasswordResetEmail(email) { success, message ->
                if (success) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
        }
    }
}
