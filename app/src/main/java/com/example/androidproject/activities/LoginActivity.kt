package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.androidproject.R
import com.example.androidproject.databinding.ActivityLoginBinding
import com.example.androidproject.viewModels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        emailEditText = findViewById(R.id.mail_tp)
        passwordEditText = findViewById(R.id.password_tp)
        loginButton = findViewById(R.id.login_btn)
        registerTextView = findViewById(R.id.register_tv)
        progressBar = findViewById(R.id.loginProgressBar)

        // Set up login button click listener
        loginButton.setOnClickListener { loginUser() }

        // Set up register text view click listener
        registerTextView.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate input
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return
        }

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                // Sign in success, navigate to main activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                // If sign in fails, display a message to the user
                Toast.makeText(
                    this@LoginActivity,
                    "Authentication failed: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
