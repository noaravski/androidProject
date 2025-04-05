package com.example.androidproject.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import com.example.androidproject.repositories.UserRepository

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileImage: CircleImageView
    private lateinit var backButton: ImageButton
    private lateinit var logoutButton: ConstraintLayout
    private lateinit var editProfileButton: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image)
        backButton = view.findViewById(R.id.back_button)
        logoutButton = view.findViewById(R.id.logout_button)
        editProfileButton = view.findViewById(R.id.edit_profile_button)

        // Load user profile image
        loadUserProfileImage()

        // Set click listeners
        backButton.setOnClickListener {
            // Navigate back
            requireActivity().onBackPressed()
        }

        logoutButton.setOnClickListener {
            // Perform logout
            logout()
        }

        editProfileButton.setOnClickListener {
            // Navigate to edit profile screen
            navigateToEditProfile()
        }
    }

    private fun loadUserProfileImage() {
        // Here you would load the user's profile image from your database or storage
        // For example, if using Firebase Storage:
        val user = auth.currentUser
        user?.let { firebaseUser ->
            // If you have a profile image URL stored in the user's profile
            firebaseUser.photoUrl?.let { uri ->
                // Use a library like Glide or Picasso to load the image
                // Example with Glide:
                // Glide.with(this).load(uri).into(profileImage)
            }
        }
    }

    private fun logout() {
        // Sign out from Firebase
        auth.signOut()

        // Show a toast message
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login screen
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToEditProfile() {
        UserRepository.instance.getUserData { user ->
            if (user != null) {
                // Create a Bundle and add the user object
                val userBundle = Bundle()
                userBundle.putParcelable("User", user)

                // Navigate to the EditUserProfileFragment with user data
                findNavController().navigate(
                    R.id.action_profileFragment_to_editUserProfileFragment,
                    userBundle
                )
            } else {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT)
                    .show()
            }
        }}
}