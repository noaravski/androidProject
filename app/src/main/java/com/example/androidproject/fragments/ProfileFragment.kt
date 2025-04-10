package com.example.androidproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.utils.ProfileImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var logoutButton: ConstraintLayout
    private lateinit var editProfileButton: ConstraintLayout
    private lateinit var profileImageView: CircleImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        logoutButton = view.findViewById(R.id.logout_button)
        editProfileButton = view.findViewById(R.id.edit_profile_button)
        profileImageView = view.findViewById(R.id.profile_image)

        // Set up click listeners
        logoutButton.setOnClickListener { logout() }
        editProfileButton.setOnClickListener { openEditProfile() }

        // Load user profile image
        loadUserProfileImage()
    }

    private fun loadUserProfileImage() {
        context?.let { ctx ->
            ProfileImageLoader.loadCurrentUserProfileImage(ctx, profileImageView)
        }
    }

    private fun logout() {
        mAuth.signOut()
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

        // Reload profile picture in case it was updated
        loadUserProfileImage()
    }
}
