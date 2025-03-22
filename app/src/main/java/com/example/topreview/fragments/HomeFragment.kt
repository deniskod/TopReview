package com.example.topreview.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.topreview.R
import com.example.topreview.adapters.ReviewAdapter
import com.example.topreview.database.DatabaseProvider
import com.example.topreview.databinding.FragmentHomeBinding
import com.example.topreview.repository.ReviewRepository
import com.example.topreview.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var userRepository: UserRepository
    private var showAllReviews = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DatabaseProvider.getDatabase(requireContext())
        reviewRepository = ReviewRepository(db.reviewDao())
        userRepository = UserRepository(db.userDao())

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) { userRepository.getAll() }
            val userMap = users.associateBy { it.uid }

            reviewAdapter = ReviewAdapter(
                emptyList(),
                currentUserId = userId,
                userMap = userMap,
                onEditClicked = { review ->
                    val action = HomeFragmentDirections.actionHomeFragmentToEditReviewFragment(review)
                    findNavController().navigate(action)
                },
                onDeleteClicked = { review ->
                    lifecycleScope.launch {
                        reviewRepository.deleteReview(review)
                        Toast.makeText(requireContext(), "Review deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            binding.recyclerViewReviews.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewReviews.adapter = reviewAdapter

            observeReviews(userId)

            binding.btnMyReviews.setOnClickListener {
                showAllReviews = !showAllReviews
                binding.btnMyReviews.text = if (showAllReviews) "My Reviews" else "Show All Reviews"
                observeReviews(userId)
            }

            binding.btnAddReview.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_addReviewFragment)
            }
        }
    }

    private fun observeReviews(userId: String) {
        if (showAllReviews) {
            reviewRepository.getAllReviews().observe(viewLifecycleOwner) { reviews ->
                reviewAdapter.updateReviews(reviews)
                if (reviews.isEmpty()) {
                    Toast.makeText(requireContext(), "No reviews available", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                val userReviews = reviewRepository.getUserReviews(userId)
                reviewAdapter.updateReviews(userReviews)
                if (userReviews.isEmpty()) {
                    Toast.makeText(requireContext(), "You have no reviews", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_profile -> {
                findNavController().navigate(R.id.action_homeFragment_to_editProfileFragment)
                true
            }
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}