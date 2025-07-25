package com.example.topreview.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.topreview.R
import com.example.topreview.adapter.ReviewsAdapter
import com.example.topreview.databinding.FragmentHomeBinding
import com.example.topreview.model.ReviewModel
import com.example.topreview.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var showAllReviews = true
    private var adapter: ReviewsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupSwipeRefresh()
        setupButtons()

        // Observe live data
        UserModel.shared.users.observe(viewLifecycleOwner, Observer {
            refreshReviewAdapter()
        })

        ReviewModel.shared.reviews.observe(viewLifecycleOwner, Observer {
            refreshReviewAdapter()
            binding.progressBar.visibility = View.GONE
        })

        ReviewModel.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding.swipeToRefresh.isRefreshing = state == ReviewModel.LoadingState.LOADING
        }

        // Initial data load
        UserModel.shared.refreshAllUsers()
        ReviewModel.shared.refreshAllReviews()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_profile -> {
                        navigateTo(R.id.action_homeFragment_to_editProfileFragment)
                        true
                    }
                    R.id.action_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        navigateTo(R.id.action_homeFragment_to_loginFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    private fun setupSwipeRefresh() {
        binding.swipeToRefresh.setOnRefreshListener {
            if (showAllReviews) {
                ReviewModel.shared.refreshAllReviews()
            } else {
                ReviewModel.shared.refreshAllUserReviews(userId)
            }
        }
    }

    private fun setupButtons() {
        binding.btnAddReview.setOnClickListener {
            navigateTo(R.id.action_homeFragment_to_addReviewFragment)
        }

        binding.btnMyReviews.setOnClickListener {
            showAllReviews = !showAllReviews
            val icon = if (showAllReviews) R.drawable.baseline_person_24 else R.drawable.ic_all
            binding.btnMyReviews.setImageResource(icon)

            if (showAllReviews) {
                ReviewModel.shared.refreshAllReviews()
            } else {
                ReviewModel.shared.refreshAllUserReviews(userId)
            }
        }
    }

    private fun refreshReviewAdapter() {
        val reviews = ReviewModel.shared.reviews.value ?: emptyList()
        val users = UserModel.shared.users.value?.associateBy { it.uid } ?: emptyMap()

        val filteredReviews = if (showAllReviews) reviews else reviews.filter { it.userId == userId }

        adapter = ReviewsAdapter(
            reviews = filteredReviews,
            currentUserId = userId,
            userMap = users,
            onEditClicked = { review ->
                val action = HomeFragmentDirections.actionHomeFragmentToEditReviewFragment(review)
                navigateTo(action)
            },
            onDeleteClicked = { review ->
                lifecycleScope.launch(Dispatchers.Main) {
                    ReviewModel.shared.delete(review) {
                        Toast.makeText(requireContext(), "Review deleted", Toast.LENGTH_SHORT).show()
                        if (showAllReviews) {
                            ReviewModel.shared.refreshAllReviews()
                        } else {
                            ReviewModel.shared.refreshAllUserReviews(userId)
                        }
                    }
                }
            }
        )

        binding.recyclerViewReviews.adapter = adapter
    }

    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    private fun navigateTo(directions: Any) {
        findNavController().navigate(directions as NavDirections)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
