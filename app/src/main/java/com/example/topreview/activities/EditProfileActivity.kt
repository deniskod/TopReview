package com.example.topreview.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.topreview.R
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSaveProfile: Button
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword) // New field for password input
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile)

        val user = auth.currentUser
        val db = DatabaseProvider.getDatabase(applicationContext)
        userRepository = UserRepository(db.userDao())

        if (user != null) {
            // Fetch user data from Room Database inside a coroutine
            lifecycleScope.launch {
                val userFromRoom = userRepository.getUserById(user.uid)
                withContext(Dispatchers.Main) {
                    if (userFromRoom != null) {
                        // Populate UI with the fetched user data
                        editTextUsername.setText(userFromRoom.username)
                        editTextEmail.setText(user.email) // Firestore email (firebaseAuth)
                    } else {
                        Toast.makeText(this@EditProfileActivity, "User not found in database", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        buttonSaveProfile.setOnClickListener {
            val newUsername = editTextUsername.text.toString()
            val newEmail = editTextEmail.text.toString()
            val password = editTextPassword.text.toString() // Get the entered password

            if (newUsername.isNotEmpty() || newEmail.isNotEmpty()) {
                if (password.isNotEmpty()) {
                    lifecycleScope.launch {
                        val userFromRoom = userRepository.getUserById(user!!.uid)

                        if (userFromRoom != null) {
                            var updatedUser = userFromRoom

                            // Always update the username if it's provided
                            if (newUsername.isNotEmpty()) {
                                updatedUser = updatedUser.copy(username = newUsername)
                            }

                            if (newEmail.isNotEmpty() && newEmail != user.email) {
                                // Re-authenticate the user before making sensitive changes
                                Log.d("logsssss","email ${user.email}")
                                Log.d("logsssss","password $password")
//                                Log.d("logsssss","password $password")
                                val credentials = EmailAuthProvider.getCredential(user.email!!, password)

                                try {
                                    user.reauthenticate(credentials).addOnCompleteListener { reAuthTask ->
                                        if (reAuthTask.isSuccessful) {
                                            // Send verification email to the new email address
                                            user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    // Verification email successfully sent
                                                    Log.d("logssss", "Verification email sent to $newEmail")
                                                    Toast.makeText(this@EditProfileActivity, "Verification email sent. Please verify your new email address.", Toast.LENGTH_SHORT).show()

                                                    // Now you can update the email in Room database (but only after email verification)
                                                    updatedUser = updatedUser!!.copy(email = newEmail)
                                                    lifecycleScope.launch {
                                                        userRepository.updateUser(updatedUser!!)

                                                        // Refetch the updated user data from Room
                                                        val updatedUserFromRoom = userRepository.getUserById(user.uid)
                                                        withContext(Dispatchers.Main) {
                                                            editTextUsername.setText(updatedUserFromRoom?.username)
                                                            editTextEmail.setText(updatedUserFromRoom?.email)
                                                            Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                                                            finish() // Navigate back to HomeActivity
                                                        }
                                                    }
                                                } else {
                                                    // Handle error when verification email could not be sent
                                                    Log.e("logssss", "Error sending verification email: ${task.exception?.message}")
                                                    Toast.makeText(this@EditProfileActivity, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            // Handle re-authentication failure
                                            Toast.makeText(this@EditProfileActivity, "Re-authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Handle other errors
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // No email change, just update the username in Room
                                userRepository.updateUser(updatedUser!!)
                                Toast.makeText(this@EditProfileActivity, "Username updated", Toast.LENGTH_SHORT).show()
                                finish() // Navigate back to HomeActivity
                            }
                        }
                    }
                }
            }
        }
    }
}
