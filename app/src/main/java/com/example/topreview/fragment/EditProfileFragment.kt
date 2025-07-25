package com.example.topreview.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.topreview.databinding.FragmentEditProfileBinding
import com.example.topreview.model.UserModel
import com.google.firebase.auth.FirebaseAuth

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            binding.imageViewProfile.setImageURI(selectedImageUri)
            binding.imageCardView.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imagePickerLauncher.launch(intent)
        }

        binding.buttonSaveProfile.setOnClickListener {
            val newFirstName = binding.editTextFirstName.text.toString().trim()
            val newLastName = binding.editTextLastName.text.toString().trim()

            if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = "$newFirstName $newLastName"

            val imageBitmap = selectedImageUri?.let {
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
            }

            val currentUser = UserModel.shared.users.value?.find { it.uid == userId }

            if (currentUser != null) {
                val updatedUser = currentUser.copy(name = fullName)
                UserModel.shared.add(updatedUser, imageBitmap) {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } else {
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            }
        }

        UserModel.shared.users.observe(viewLifecycleOwner, Observer { users ->
            val currentUser = users.find { it.uid == userId }
            currentUser?.let { user ->
                val names = user.name.split(" ")
                binding.editTextFirstName.setText(names.getOrNull(0) ?: "")
                binding.editTextLastName.setText(names.getOrNull(1) ?: "")
                currentImageUrl = user.imageUrl

                if (!currentImageUrl.isNullOrBlank()) {
                    Glide.with(requireContext())
                        .load(currentImageUrl)
                        .into(binding.imageViewProfile)
                    binding.imageCardView.visibility = View.VISIBLE
                }
            }
        })

        UserModel.shared.getUserById(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
