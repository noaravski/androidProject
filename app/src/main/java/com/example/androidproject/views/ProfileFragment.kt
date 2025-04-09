package com.example.androidproject.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.androidproject.R
import com.example.androidproject.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var logoutButton: ConstraintLayout
    private lateinit var editProfileButton: ConstraintLayout
    private lateinit var mAuth: FirebaseAuth

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
        mAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        logoutButton = view.findViewById(R.id.logout_button)
        editProfileButton = view.findViewById(R.id.edit_profile_button)

        // Set up click listeners
        logoutButton.setOnClickListener { logout() }
        editProfileButton.setOnClickListener { openEditProfile() }
    }

    private fun logout() {
        mAuth.signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login activity
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        activity?.finish()
    }

    private fun openEditProfile() {
        // Navigate to edit profile fragment
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, EditUserProfileFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
