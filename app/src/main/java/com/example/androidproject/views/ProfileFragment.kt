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
    private lateinit var logoutButton: ConstraintLayout
    private lateinit var editProfileButton: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        profileImage = view.findViewById(R.id.profile_image)
        logoutButton = view.findViewById(R.id.logout_button)
        editProfileButton = view.findViewById(R.id.edit_profile_button)

        loadUserProfileImage()


        logoutButton.setOnClickListener {
            logout()
        }

        editProfileButton.setOnClickListener {
            navigateToEditProfile()
        }
    }

    private fun loadUserProfileImage() {
        val user = auth.currentUser
        user?.let { firebaseUser ->
            firebaseUser.photoUrl?.let { uri ->
            }
        }
    }

    private fun logout() {
        auth.signOut()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToEditProfile() {
        UserRepository.instance.getUserData { user ->
            if (user != null) {
                val userBundle = Bundle()
                userBundle.putParcelable("User", user)

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