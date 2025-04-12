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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID

class CreateGroupFragment : Fragment() {

    private lateinit var groupNameEditText: EditText
    private lateinit var groupDescriptionEditText: EditText
    private lateinit var uploadButton: CircleImageView
    private lateinit var submitButton: Button

    private var selectedImageUri: Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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
            uploadButton.setImageURI(selectedImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        groupNameEditText = view.findViewById(R.id.etGroupName)
        groupDescriptionEditText = view.findViewById(R.id.etGroupDescription)
        uploadButton = view.findViewById(R.id.btnUpload)
        submitButton = view.findViewById(R.id.btnSubmit)

        // Set up click listeners
        uploadButton.setOnClickListener { checkCameraPermission() }
        submitButton.setOnClickListener { createGroup() }
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
                .setMessage("This app needs camera access to take photos for your group.")
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
                put(MediaStore.Images.Media.TITLE, "New Group Image")
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

    private fun createGroup() {
        val groupName = groupNameEditText.text.toString().trim()
        val groupDescription = groupDescriptionEditText.text.toString().trim()

        if (groupName.isEmpty()) {
            groupNameEditText.error = "Group name is required"
            return
        }

        // Create group in Firestore
        val groupId = UUID.randomUUID().toString()
        val userId = auth.currentUser?.uid ?: return

        val group = HashMap<String, Any>()
        group["groupName"] = groupName
        group["description"] = groupDescription
        group["createdBy"] = userId // Store as String
        group["createdAt"] = System.currentTimeMillis()
        group["currency"] = "USD" // Default currency
        group["imageUrl"] = "" // Default currency

        // Initialize members list with the creator
        val members = ArrayList<String>()
        members.add(userId)
        group["members"] = members

        if (selectedImageUri != null) {
            // Show loading state
            submitButton.isEnabled = false
            submitButton.text = "Creating..."

            // Upload image to Cloudinary
            context?.let { ctx ->
                CloudinaryHelper.uploadImage(
                    ctx,
                    selectedImageUri!!,
                    "group_images",
                    onSuccess = { imageUrl ->
                        group["imageUrl"] = imageUrl
                        saveGroupToFirestore(groupId, group)
                    },
                    onError = { error ->
                        Toast.makeText(context, "Failed to upload image: $error", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                        submitButton.text = "Create"
                    }
                )
            }
        } else {
            group["imageUrl"] = "default"
            saveGroupToFirestore(groupId, group)
        }
    }

    private fun saveGroupToFirestore(groupId: String, group: Map<String, Any>) {
        db.collection("groups").document(groupId)
            .set(group)
            .addOnSuccessListener {
                Toast.makeText(context, "Group created successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to MainFragment
                findNavController().navigate(R.id.action_createGroupFragment_to_mainFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error creating group: ${e.message}", Toast.LENGTH_SHORT).show()
                submitButton.isEnabled = true
                submitButton.text = "Create"
            }
    }
}
