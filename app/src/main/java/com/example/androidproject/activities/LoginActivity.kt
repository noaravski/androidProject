package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.androidproject.databinding.ActivityLoginBinding
import com.example.androidproject.repositories.UserRepository
import com.example.androidproject.viewModels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private var binding: ActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
//        onNotSingedIn()
        binding!!.loginProgressBar.visibility = View.GONE

        viewModel.loginResult.observe(this, Observer { loggedIn ->
            if (loggedIn) {
                binding!!.loginProgressBar.visibility = View.GONE

                // User is already logged in, navigate to another activity
                changeActivity(MainActivity::class.java)

                // Finish this activity to prevent the user from navigating back to the login screen
                finish()
            }
        })

        viewModel.authenticationFailed.observe(this, Observer
        {
            if (it) {
                // Show toast message for authentication failure
                showToast("can not log you in.")
                binding!!.loginProgressBar.visibility = View.GONE
            }
        })

        onLogin()
        binding!!.loginProgressBar.visibility = View.GONE
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (viewModel.isConnected()) {
            changeActivity(MainActivity::class.java)
        }
    }


//    private fun onNotSingedIn() {
//        binding!!.signinTv.setOnClickListener {
//            changeActivity(
//                SigninActivity::class.java
//            )
//        }
//    }

    private fun onLogin() {
        binding!!.loginBtn.setOnClickListener {
            val mail: String = binding!!.mailTp.text.toString()
            val password: String = binding!!.passwordTp.text.toString()

            if (TextUtils.isEmpty(mail)) {
                showToast("enter mail")
            } else if (TextUtils.isEmpty(password)) {
                showToast("enter password")
            } else {
                binding!!.loginProgressBar.visibility = View.VISIBLE
                viewModel.login(mail,password)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun changeActivity(activityClass: Class<*>) {
        val intent = Intent(this@LoginActivity, activityClass)
        startActivity(intent)
    }
}