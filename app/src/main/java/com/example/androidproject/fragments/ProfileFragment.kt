package com.example.androidproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.utils.ProfileImageLoader
import com.example.androidproject.viewmodel.ProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var logoutButton: ConstraintLayout
    private lateinit var editProfileButton: ConstraintLayout
    private lateinit var profileImageView: CircleImageView
    private lateinit var usernameTextView: TextView
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Initialize UI elements
        logoutButton = view.findViewById(R.id.logout_button)
        editProfileButton = view.findViewById(R.id.edit_profile_button)
        profileImageView = view.findViewById(R.id.profile_image)
        usernameTextView = view.findViewById(R.id.username_text)

        // Set up click listeners
        logoutButton.setOnClickListener { logout() }
        editProfileButton.setOnClickListener { openEditProfile() }

        // Set up observers
        setupObservers()

        // Load user profile image and data
        loadUserProfileImage()
        viewModel.loadUserData()
    }

    private fun setupObservers() {
        // Observe user data
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            userData?.let {
                // Make sure we're getting the username from the userData map
                val username = it["Username"] as? String ?: "User"
                usernameTextView.text = username
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You could show a loading indicator here if needed
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserProfileImage() {
        context?.let { ctx ->
            ProfileImageLoader.loadCurrentUserProfileImage(ctx, profileImageView)
        }
    }

    private fun logout() {
        viewModel.logout()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login fragment
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    private fun openEditProfile() {
        // Navigate to edit profile fragment
        findNavController().navigate(R.id.action_profileFragment_to_editUserProfileFragment)
    }

    override fun onResume() {
        super.onResume()

        // Reload profile picture and user data in case it was updated
        loadUserProfileImage()
        viewModel.loadUserData()
    }
}
