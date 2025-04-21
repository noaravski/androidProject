package com.example.androidproject.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidproject.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val context = application.applicationContext

    // LiveData for user data
    private val _userData = MutableLiveData<Map<String, Any>>()
    val userData: LiveData<Map<String, Any>> = _userData

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Load user data
    fun loadUserData() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _errorMessage.value = "User not logged in"
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    _userData.value = data
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error loading user data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Update user profile
    fun updateUserProfile(
        username: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _errorMessage.value = "User not logged in"
            _isLoading.value = false
            onError("User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                val userData = HashMap<String, Any>()
                userData["Username"] = username

                if (imageUri != null) {
                    // Upload image to Cloudinary
                    val imageUrl = uploadImageToCloudinary(imageUri)
                    userData["ImgUrl"] = imageUrl
                }

                // Update user in Firestore
                db.collection("users").document(userId).update(userData).await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Error updating profile: ${e.message}"
                _isLoading.value = false
                onError("Error updating profile: ${e.message}")
            }
        }
    }

    // Upload image to Cloudinary
    private suspend fun uploadImageToCloudinary(imageUri: Uri): String = suspendCoroutine { continuation ->
        CloudinaryHelper.uploadImage(
            context,
            imageUri,
            "profile_images",
            onSuccess = { imageUrl ->
                continuation.resume(imageUrl)
            },
            onError = { error ->
                continuation.resumeWithException(Exception(error))
            }
        )
    }

    // Logout user
    fun logout() {
        auth.signOut()
    }
}
