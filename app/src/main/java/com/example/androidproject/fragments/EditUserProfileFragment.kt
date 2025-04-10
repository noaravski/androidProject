package com.example.androidproject.fragments

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.utils.CloudinaryHelper
import com.example.androidproject.utils.ProfileImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.HashMap

class EditUserProfileFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImageView: CircleImageView

    private var selectedImageUri: Uri? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            profileImageView.setImageURI(selectedImageUri)
        }
    }

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
        profileImageView = view.findViewById(R.id.user_img)

        // Set up click listeners
        profileImageView.setOnClickListener { checkCameraPermission() }
        saveButton.setOnClickListener { saveUserData() }

        // Load user data
        loadUserData()
    }

    private fun checkCameraPermission() {
        context?.let { ctx ->
            when {
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    openCamera()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    showCameraPermissionRationale()
                }
                else -> {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    private fun showCameraPermissionRationale() {
        context?.let { ctx ->
            AlertDialog.Builder(ctx)
                .setTitle("Camera Permission Required")
                .setMessage("This app needs camera access to take your profile photo.")
                .setPositiveButton("Grant") { _, _ ->
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun openCamera() {
        context?.let { ctx ->
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "New Profile Image")
                put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            }

            val imageUri = ctx.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            imageUri?.let {
                selectedImageUri = it
                takePictureLauncher.launch(it)
            }
        }
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
                    val username = documentSnapshot.getString("Username")

                    if (username != null) {
                        usernameEditText.setText(username)
                    }

                    // Load profile image
                    context?.let { ctx ->
                        ProfileImageLoader.loadCurrentUserProfileImage(ctx, profileImageView)
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
        userData["Username"] = username

        if (selectedImageUri != null) {
            // Show loading state
            saveButton.isEnabled = false
            saveButton.text = "Saving..."

            // Upload image to Cloudinary
            context?.let { ctx ->
                CloudinaryHelper.uploadImage(
                    ctx,
                    selectedImageUri!!,
                    "profile_images",
                    onSuccess = { imageUrl ->
                        userData["ImgUrl"] = imageUrl
                        updateUserInFirestore(userId, userData)
                    },
                    onError = { error ->
                        Toast.makeText(context, "Failed to upload image: $error", Toast.LENGTH_SHORT).show()
                        saveButton.isEnabled = true
                        saveButton.text = "Save"
                    }
                )
            }
        } else {
            updateUserInFirestore(userId, userData)
        }
    }

    private fun updateUserInFirestore(userId: String, userData: Map<String, Any>) {
        db.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // Navigate back to profile fragment
                findNavController().navigate(R.id.action_editUserProfileFragment_to_profileFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
                saveButton.text = "Save"
            }
    }
}
