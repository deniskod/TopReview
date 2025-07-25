package com.example.topreview.fragment

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.topreview.R
import com.example.topreview.databinding.FragmentSignUpBinding
import com.example.topreview.model.AuthModel
import com.example.topreview.model.User
import com.example.topreview.model.UserModel
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val authModel: AuthModel by viewModels()
    private var imageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            binding.imageViewSelected.apply {
                setImageURI(imageUri)
                visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.signUpButton.setOnClickListener {
            handleSignUp()
        }

        binding.loginText.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun handleSignUp() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()

        when {
            email.isEmpty()      -> showToast("Email is required")
            password.isEmpty()   -> showToast("Password is required")
            firstName.isEmpty()  -> showToast("First name is required")
            lastName.isEmpty()   -> showToast("Last name is required")
            imageUri == null     -> showToast("Profile image is required")
            else -> performRegistration(email, password, firstName, lastName)
        }
    }

    private fun performRegistration(email: String, password: String, firstName: String, lastName: String) {
        authModel.signUp(email, password) { success, message ->
            if (!success) {
                showToast(message)
                return@signUp
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId.isNullOrBlank()) {
                showToast("Something went wrong. Please try again.")
                return@signUp
            }

            val fullName = "$firstName $lastName"
            val bitmap = try {
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            } catch (e: Exception) {
                showToast("Failed to load selected image.")
                return@signUp
            }

            val newUser = User(
                uid = userId,
                name = fullName,
                imageUrl = ""
            )

            binding.signUpButton.isEnabled = false

            UserModel.shared.add(newUser, bitmap) {
                showToast("User registered successfully!")
                findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
                binding.signUpButton.isEnabled = true
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
