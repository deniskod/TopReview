package com.example.topreview.fragments

import AuthViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.topreview.R
import com.example.topreview.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.signIn(email, password) { success, message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val email = binding.emailEditText.text.toString().trim()

        if (email.isNotEmpty()) {
            authViewModel.sendPasswordResetEmail(email) { success, message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please enter your email first", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
