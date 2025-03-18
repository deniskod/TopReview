package com.example.topreview.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.topreview.R
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnCancel = findViewById(R.id.btnCancel)

        loadUserData()

        btnSubmit.setOnClickListener {
            hideKeyboard()
            updateUserData()
        }

        btnCancel.setOnClickListener {
            hideKeyboard()
            finish()
        }
    }

    private fun loadUserData() {
        val user = firebaseAuth.currentUser
        user?.let {
            etEmail.setText(it.email)
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    etUsername.setText(document.getString("username") ?: "")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Error loading username", e)
                    Toast.makeText(this, "Failed to load username", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserData() {
        val user = firebaseAuth.currentUser
        val newUsername = etUsername.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val currentEmail = user.email
        val userRef = db.collection("users").document(user.uid)

        user.providerData.forEach { profile ->
            val providerId = profile.providerId
            Log.d("FirebaseAuth", "User signed in with: $providerId")

            if (newEmail != currentEmail) {
                if (providerId == "google.com") {
                    reauthenticateWithGoogle(user, newUsername, newEmail, userRef)
                } else {
                    promptForPassword(user, newUsername, newEmail, userRef)
                }
            } else {
                updateFirestoreOnly(user, newUsername, userRef)
            }
        }
    }

    private fun reauthenticateWithGoogle(user: FirebaseUser, newUsername: String, newEmail: String, userRef: DocumentReference) {
        val googleProvider = GoogleAuthProvider.getCredential(null, null)

        user.reauthenticate(googleProvider).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                updateEmailAndFirestore(user, newUsername, newEmail, userRef)
            } else {
                Toast.makeText(this, "Google re-authentication failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun promptForPassword(user: FirebaseUser, newUsername: String, newEmail: String, userRef: DocumentReference) {
        hideKeyboard()

        val passwordInput = EditText(this)
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val dialog = AlertDialog.Builder(this)
            .setTitle("Re-authenticate")
            .setMessage("Please enter your password to update your email.")
            .setView(passwordInput)
            .setPositiveButton("Confirm") { _, _ ->
                hideKeyboard()
                val password = passwordInput.text.toString()

                if (password.isNotEmpty()) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, password)
                    user.reauthenticate(credential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Log.d("FirebaseAuth", "Re-authentication successful")
                            updateEmailAndFirestore(user, newUsername, newEmail, userRef)
                        } else {
                            val errorMessage = authTask.exception?.message ?: "Unknown error"
                            Log.e("FirebaseAuthError", "Re-authentication failed: $errorMessage")
                            if (errorMessage.contains("expired") || errorMessage.contains("requires recent authentication")) {
                                forceLogout()
                            } else {
                                Toast.makeText(this, "Re-authentication failed: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                hideKeyboard()
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun updateEmailAndFirestore(user: FirebaseUser, newUsername: String, newEmail: String, userRef: DocumentReference) {
        user.updateEmail(newEmail).addOnCompleteListener { emailTask ->
            if (emailTask.isSuccessful) {
                updateFirestoreOnly(user, newUsername, userRef)
            } else {
                Toast.makeText(this, "Failed to update email: ${emailTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFirestoreOnly(user: FirebaseUser, newUsername: String, userRef: DocumentReference) {
        val updates: Map<String, Any> = mapOf(
            "username" to newUsername,
            "email" to (user.email ?: "")
        )

        userRef.update(updates).addOnCompleteListener { firestoreTask ->
            if (firestoreTask.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                goToHome()
            } else {
                Toast.makeText(this, "Failed to update Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun forceLogout() {
        firebaseAuth.signOut()
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    private fun goToHome() {
        hideKeyboard()
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.postDelayed({
            view.requestFocus()
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    override fun onBackPressed() {
        hideKeyboard()
        super.onBackPressed()
    }
}
