package com.example.androidproject.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidproject.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _authenticationFailed = MutableLiveData<Boolean>()
    val authenticationFailed: LiveData<Boolean> = _authenticationFailed

    fun login(mail: String, password: String) {
        mAuth.signInWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    UserRepository.instance.setUserId(task.result.user!!.uid)
                    _loginResult.value = true
                    println("should login")
                } else {
                    println("should not login")
                    _loginResult.value = false
                    _authenticationFailed.value = true
                }
            }
    }

    fun isConnected(): Boolean {
        val currentUser = mAuth.currentUser

        if (currentUser != null) {
            UserRepository.instance.setUserId(currentUser.uid)
            return true
        }

        return false
    }
}