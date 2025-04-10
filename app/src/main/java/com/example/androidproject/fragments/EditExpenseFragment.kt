package com.example.androidproject.fragments

import android.Manifest
import android.app.DatePickerDialog
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
import android.widget.ImageView
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
import com.example.androidproject.utils.ProfileImageLoader.Companion.convertToHttps
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditExpenseFragment : Fragment() {

    private lateinit var descriptionField: EditText
    private lateinit var amountField: EditText
    private lateinit var dateField: EditText
    private lateinit var expenseImage: ImageView
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private lateinit var db: FirebaseFirestore
    private var expenseDate: Long = 0
    private var groupId: String = ""
    private var expenseId: String = ""
    private var currentImageUrl: String? = null
    private var selectedImageUri: Uri? = null

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(
                context, "Camera permission is required to take photos", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            expenseImage.setImageURI(selectedImageUri)
            uploadImageToCloudinary()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get expense ID from arguments
        val args: EditExpenseFragmentArgs by navArgs()
        expenseId = args.expenseId

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        descriptionField = view.findViewById(R.id.descriptionField)
        amountField = view.findViewById(R.id.amountField)
        dateField = view.findViewById(R.id.dateField)
        expenseImage = view.findViewById(R.id.expenseImage)
        saveButton = view.findViewById(R.id.saveButton)
        deleteButton = view.findViewById(R.id.deleteButton)


        // Set up date picker
        dateField.setOnClickListener {
            showDatePicker()
        }

        // Set up click listener for expense image
        expenseImage.setOnClickListener {
            checkCameraPermission()
        }

        // Set up save button
        saveButton.setOnClickListener {
            saveExpenseChanges(expenseId)
        }

        // Set up delete button
        deleteButton.setOnClickListener {
            deleteExpense(expenseId)
        }

        // Load expense data
        loadExpenseData(expenseId)
    }

    private fun loadExpenseData(expenseId: String) {
        db.collection("expenses").document(expenseId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get expense data
                    val description = document.getString("description") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    expenseDate = document.getLong("date") ?: System.currentTimeMillis()
                    currentImageUrl = document.getString("imgUrl")
                    groupId = document.getString("groupName") ?: ""

                    // Set fields
                    descriptionField.setText(description)
                    amountField.setText(String.format("%.2f", amount))
                    // Set date
                    calendar.timeInMillis = expenseDate
                    dateField.setText(dateFormat.format(calendar.time))

                    // Load image
                    if (!currentImageUrl.isNullOrEmpty()) {
                        context?.let { ctx ->
                            Glide.with(ctx).load(convertToHttps(currentImageUrl.toString())).placeholder(R.drawable.ic_recipt)
                                .error(R.drawable.ic_recipt).into(expenseImage)
                        }
                    } else {
                        expenseImage.setImageResource(R.drawable.ic_recipt)
                    }
                } else {
                    Toast.makeText(context, "Expense not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error loading expense: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }
    }

    private fun checkCameraPermission() {
        context?.let { ctx ->
            when {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
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
            AlertDialog.Builder(ctx).setTitle("Camera Permission Required")
                .setMessage("This app needs camera access to take photos for your expenses.")
                .setPositiveButton("Grant") { _, _ ->
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }.setNegativeButton("Cancel", null).show()
        }
    }

    private fun openCamera() {
        context?.let { ctx ->
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "New Expense Image")
                put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            }

            val imageUri = ctx.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            )

            imageUri?.let {
                selectedImageUri = it
                takePictureLauncher.launch(it)

                context?.let { ctx ->
                    Glide.with(ctx).load(convertToHttps(selectedImageUri.toString())).placeholder(R.drawable.ic_recipt)
                        .error(R.drawable.ic_recipt).into(expenseImage)
                }
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
            CloudinaryHelper.uploadImage(ctx,
                selectedImageUri!!,
                "expense_images",
                onSuccess = { imageUrl ->
                    // Update the expense in Firestore
                    db.collection("expenses").document(expenseId).update("imgUrl", imageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Expense image updated", Toast.LENGTH_SHORT)
                                .show()
                            currentImageUrl = imageUrl
                            saveButton.isEnabled = true
                            saveButton.text = "Save Changes"
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error updating expense image: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            saveButton.isEnabled = true
                            saveButton.text = "Save Changes"
                        }
                },
                onError = { error ->
                    Toast.makeText(context, "Failed to upload image: $error", Toast.LENGTH_SHORT)
                        .show()
                    saveButton.isEnabled = true
                    saveButton.text = "Save Changes"
                })
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        context?.let { ctx ->
            DatePickerDialog(ctx, { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                expenseDate = calendar.timeInMillis
                dateField.setText(dateFormat.format(calendar.time))
            }, year, month, day).show()
        }
    }

    private fun saveExpenseChanges(expenseId: String) {
        val description = descriptionField.text.toString().trim()
        val amountStr = amountField.text.toString().trim()

        if (description.isEmpty()) {
            descriptionField.error = "Description is required"
            return
        }

        if (amountStr.isEmpty()) {
            amountField.error = "Amount is required"
            return
        }

        val amount: Double
        try {
            amount = amountStr.toDouble()
        } catch (e: NumberFormatException) {
            amountField.error = "Invalid amount"
            return
        }

        // Disable save button
        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        // Update expense in Firestore
        val updates = hashMapOf<String, Any>(
            "description" to description,
            "amount" to amount,
            "date" to expenseDate,
            "imgUrl" to (currentImageUrl ?: "")
        )

        db.collection("expenses").document(expenseId).update(updates).addOnSuccessListener {
                Toast.makeText(context, "Expense updated successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to group expenses
                val action =
                    EditExpenseFragmentDirections.actionEditExpenseFragmentToGroupExpensesFragment(
                        groupId
                    )
                findNavController().navigate(action)
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error updating expense: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                saveButton.isEnabled = true
                saveButton.text = "Save Changes"
            }
    }

    private fun deleteExpense(expenseId: String) {
        // Disable delete button
        deleteButton.isEnabled = false
        deleteButton.text = "Deleting..."

        // Delete expense from Firestore
        db.collection("expenses").document(expenseId).delete().addOnSuccessListener {
                Toast.makeText(context, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to group expenses
                val action =
                    EditExpenseFragmentDirections.actionEditExpenseFragmentToGroupExpensesFragment(
                        groupId
                    )
                findNavController().navigate(action)
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting expense: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                deleteButton.isEnabled = true
                deleteButton.text = "Delete Expense"
            }
    }
}
