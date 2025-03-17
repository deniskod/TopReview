package com.example.topreview

import AuthViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.topreview.activities.HomeActivity
import com.example.topreview.activities.LoginActivity
import com.example.topreview.ui.theme.TopReviewTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        if (authViewModel.isUserSignedIn()) {
            // Navigate to the home screen or user profile
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // Navigate to the login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Close MainActivity after redirect
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TopReviewTheme {
        Greeting("Android")
    }
}