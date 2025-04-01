package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
        Log.d("LoginActivity", "onCreate")
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        viewModel.loginResult.observe(this, Observer { loggedIn ->
            if (loggedIn) {
                binding!!.loginProgressBar.visibility = View.GONE

                changeActivity(MainActivity::class.java)

                finish()
            }
        })

        viewModel.authenticationFailed.observe(this, Observer
        {
            if (it) {
                showToast("can not log you in.")
                binding!!.loginProgressBar.visibility = View.GONE
            }
        })

        onLogin()
        binding!!.loginProgressBar.visibility = View.GONE
    }

    public override fun onStart() {
        super.onStart()
        if (viewModel.isConnected()) {
            changeActivity(MainActivity::class.java)
        }
    }

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