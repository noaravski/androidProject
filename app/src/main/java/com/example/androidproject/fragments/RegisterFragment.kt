package com.example.androidproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class RegisterFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        emailEditText = view.findViewById(R.id.mail_tp_register)
        usernameEditText = view.findViewById(R.id.username_tp_register)
        passwordEditText = view.findViewById(R.id.password_tp_register)
        registerButton = view.findViewById(R.id.register_btn)
        progressBar = view.findViewById(R.id.registerProgressBar)

        // Set up register button click listener
        registerButton.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate input
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }

        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            usernameEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return
        }

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User created successfully, save additional info to Firestore
                    val userId = mAuth.currentUser?.uid
                    if (userId != null) {
                        val user = HashMap<String, Any>()
                        user["Username"] = username
                        user["Mail"] = email
                        user["ImgUrl"] = ""

                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                Toast.makeText(context, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
