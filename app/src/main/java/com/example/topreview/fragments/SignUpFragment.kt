package com.example.topreview.fragments

import AuthViewModel
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.topreview.R
import com.example.topreview.databinding.FragmentSignUpBinding
import com.example.topreview.repository.UserRepository
import com.example.topreview.utils.FirebaseHelper
import com.example.topreview.database.DatabaseProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var userRepository: UserRepository
    private var imageUri: Uri? = null

    private val authViewModel: AuthViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            binding.imageViewSelected.setImageURI(imageUri)
            binding.imageViewSelected.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DatabaseProvider.getDatabase(requireContext())
        userRepository = UserRepository(db.userDao())

        binding.buttonSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()

            when {
                email.isEmpty() -> showToast("Email is required")
                password.isEmpty() -> showToast("Password is required")
                firstName.isEmpty() -> showToast("First name is required")
                lastName.isEmpty() -> showToast("Last name is required")
                imageUri == null -> showToast("Profile image is required")
                else -> {
                    authViewModel.signUp(email, password) { success, message ->
                        if (success) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                            FirebaseHelper.uploadImageToFirebaseStorage(imageUri!!, userId) { imageUrl ->
                                saveUserAndNavigate(userId, firstName, lastName, imageUrl)
                            }
                        } else {
                            showToast(message)
                        }
                    }
                }
            }
        }

        binding.loginText.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun saveUserAndNavigate(userId: String, firstName: String, lastName: String, imageUrl: String) {
        val fullName = "$firstName $lastName"
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = userRepository.saveUserToFirestore(userId,fullName,imageUrl)
                if (result != null) {
                    android.util.Log.d("SignUpFragment", "User successfully added with rowId: $result")
                } else {
                    android.util.Log.e("SignUpFragment", "Insert failed (rowId <= 0)")
                }
            } catch (e: Exception) {
                android.util.Log.e("SignUpFragment", "Insert exception: ${e.message}", e)
            }
        }

        requireActivity().runOnUiThread {
            showToast("User registered successfully!")
            findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
