package com.example.topreview.activities

import AuthViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.example.topreview.Greeting
import com.example.topreview.ui.theme.TopReviewTheme

class HomeActivity : ComponentActivity() {

    // Get the instance of the AuthViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TopReviewTheme {
                // Define your Home screen content here
                Column {
                    Greeting("Welcome to Home")

                    // Add a spacer for some space between components
                    Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

                    // Add a logout button
                    Button(onClick = {
                        // Sign out the user
                        authViewModel.signOut()

                        // Navigate to the Login screen after logout
                        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // Finish HomeActivity so it doesn't stay in the back stack
                    }) {
                        // Button text
                        androidx.compose.material3.Text("Logout")
                    }
                }
            }
        }
    }
}
