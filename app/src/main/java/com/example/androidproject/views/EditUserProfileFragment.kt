package com.example.androidproject.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class EditUserProfileFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        emailEditText = view.findViewById(R.id.mail_tp)
        usernameEditText = view.findViewById(R.id.username_edit_tp)
        saveButton = view.findViewById(R.id.save_edit_btn)

        // Load user data
        loadUserData()

        // Set up click listener
        saveButton.setOnClickListener { saveUserData() }
    }

    private fun loadUserData() {
        val userId = mAuth.currentUser?.uid ?: return

        // Set email from Firebase Auth
        emailEditText.setText(mAuth.currentUser?.email)

        // Get username from Firestore
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val username = documentSnapshot.getString("username")
                    if (username != null) {
                        usernameEditText.setText(username)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val username = usernameEditText.text.toString().trim()

        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            return
        }

        val userId = mAuth.currentUser?.uid ?: return

        val userData = HashMap<String, Any>()
        userData["username"] = username

        db.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // Navigate back to profile fragment
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainer, ProfileFragment())
                transaction.commit()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
