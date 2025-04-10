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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.example.androidproject.utils.CloudinaryHelper
import com.example.androidproject.utils.ProfileImageLoader
import com.example.androidproject.utils.ProfileImageLoader.Companion.convertToHttps
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class EditGroupFragment : Fragment() {

    private lateinit var groupNameEditText: EditText
    private lateinit var groupDescriptionEditText: EditText
    private lateinit var currencySpinner: Spinner
    private lateinit var groupImage: CircleImageView
    private lateinit var saveButton: Button
    private lateinit var groupNameLabel: TextView
    private lateinit var groupDescriptionLabel: TextView
    private lateinit var currencyLabel: TextView

    private lateinit var db: FirebaseFirestore
    private var currentCurrency = "USD"
    private var currentImageUrl: String? = null
    private var selectedImageUri: Uri? = null
    private var groupId: String = ""

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
            groupImage.setImageURI(selectedImageUri)
            Glide.with(this).load(convertToHttps(selectedImageUri.toString())).into(groupImage)
            uploadImageToCloudinary()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get group ID from arguments
        val args: EditGroupFragmentArgs by navArgs()
        groupId = args.groupId

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        groupNameEditText = view.findViewById(R.id.etGroupName)
        groupDescriptionEditText = view.findViewById(R.id.etGroupDescription)
        currencySpinner = view.findViewById(R.id.currencySpinner)
        groupImage = view.findViewById(R.id.groupImage)
        saveButton = view.findViewById(R.id.btnSave)
        groupNameLabel = view.findViewById(R.id.groupNameLabel)
        groupDescriptionLabel = view.findViewById(R.id.groupDescriptionLabel)
        currencyLabel = view.findViewById(R.id.currencyLabel)

        // Set up currency spinner
        setupCurrencySpinner()

        // Set up click listener for group image
        groupImage.setOnClickListener {
            checkCameraPermission()
            loadGroupData(groupId)

        }

        // Set up save button
        saveButton.setOnClickListener {
            saveGroupChanges(groupId)
        }

        // Load group data
        loadGroupData(groupId)
    }

    private fun loadUserProfileImage(groupId: String) {
        context?.let { ctx ->
            ProfileImageLoader.loadGroupImage(ctx,groupId, groupImage)
        }
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "ILS")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter
    }

    private fun loadGroupData(groupId: String) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get group data
                    val groupName = document.getString("groupName") ?: document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    currentCurrency = document.getString("currency") ?: "USD"
                    currentImageUrl = document.getString("imageUrl")

                    // Set fields
                    groupNameEditText.setText(groupName)
                    groupDescriptionEditText.setText(description)

                    // Set currency spinner
                    val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "ILS")
                    val currencyPosition = currencies.indexOf(currentCurrency)
                    if (currencyPosition >= 0) {
                        currencySpinner.setSelection(currencyPosition)
                    } else {
                        // Default to USD if currency not found
                        val usdPosition = currencies.indexOf("USD")
                        if (usdPosition >= 0) {
                            currencySpinner.setSelection(usdPosition)
                        }
                    }

                    loadUserProfileImage(groupId)
                } else {
                    Toast.makeText(context, "Group not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading group: ${e.message}", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
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

    private fun uploadImageToCloudinary() {
        if (selectedImageUri == null) return

        // Show loading state
        saveButton.isEnabled = false
        saveButton.text = "Uploading image..."

        // Upload image to Cloudinary
        context?.let { ctx ->
            CloudinaryHelper.uploadImage(
                ctx,
                selectedImageUri!!,
                "group_images",
                onSuccess = { imageUrl ->
                    // Update the current image URL
                    currentImageUrl = imageUrl

                    // Update the group in Firestore
                    db.collection("groups").document(groupId)
                        .update("imageUrl", imageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Group image updated", Toast.LENGTH_SHORT).show()
                            saveButton.isEnabled = true
                            saveButton.text = "Save Changes"
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error updating group image: ${e.message}", Toast.LENGTH_SHORT).show()
                            saveButton.isEnabled = true
                            saveButton.text = "Save Changes"
                        }
                },
                onError = { error ->
                    Toast.makeText(context, "Failed to upload image: $error", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                    saveButton.text = "Save Changes"
                }
            )
        }
    }

    private fun saveGroupChanges(groupId: String) {
        val groupName = groupNameEditText.text.toString().trim()
        val description = groupDescriptionEditText.text.toString().trim()
        val currency = currencySpinner.selectedItem.toString()

        if (groupName.isEmpty()) {
            groupNameEditText.error = "Group name is required"
            return
        }

        // Disable save button
        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        // Update group in Firestore
        val updates = hashMapOf<String, Any>(
            "groupName" to groupName,
            "description" to description,
            "currency" to currency,
            "imageUrl" to (currentImageUrl ?: "default")
        )

        db.collection("groups").document(groupId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Group updated successfully", Toast.LENGTH_SHORT).show()

                // If currency changed, update expenses
                if (currency != currentCurrency) {
                    updateExpensesCurrency(groupId, currency)
                } else {
                    // Navigate back to group expenses
                    val action = EditGroupFragmentDirections.actionEditGroupFragmentToGroupExpensesFragment(groupId)
                    findNavController().navigate(action)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating group: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
                saveButton.text = "Save Changes"
            }
    }

    private fun updateExpensesCurrency(groupId: String, newCurrency: String) {
        // Get all expenses for this group
        db.collection("expenses")
            .whereEqualTo("groupName", groupId)
            .get()
            .addOnSuccessListener { documents ->
                // Update each expense with the new currency
                for (document in documents) {
                    db.collection("expenses").document(document.id)
                        .update("currency", newCurrency)
                }

                // Navigate back to group expenses
                val action = EditGroupFragmentDirections.actionEditGroupFragmentToGroupExpensesFragment(groupId)
                findNavController().navigate(action)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating expenses: ${e.message}", Toast.LENGTH_SHORT).show()
                // Navigate back to group expenses anyway
                val action = EditGroupFragmentDirections.actionEditGroupFragmentToGroupExpensesFragment(groupId)
                findNavController().navigate(action)
            }
    }
}
