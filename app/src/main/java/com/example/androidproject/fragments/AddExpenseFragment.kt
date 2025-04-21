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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidproject.R
import com.example.androidproject.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap
import java.util.UUID

class AddExpenseFragment : Fragment() {

    private lateinit var amountField: EditText
    private lateinit var descriptionField: EditText
    private lateinit var uploadButton: ImageView
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
        return inflater.inflate(R.layout.fragment_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get group ID from arguments
        val args: AddExpenseFragmentArgs by navArgs()
        val groupId = args.groupId

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        amountField = view.findViewById(R.id.amountField)
        descriptionField = view.findViewById(R.id.descriptionField)
        uploadButton = view.findViewById(R.id.uploadButton)
        submitButton = view.findViewById(R.id.submitButton)

        // Set up click listeners
        uploadButton.setOnClickListener { checkCameraPermission() }
        submitButton.setOnClickListener { addExpense(groupId) }
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
                .setMessage("This app needs camera access to take photos for your expenses.")
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
                put(MediaStore.Images.Media.TITLE, "New Expense Image")
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

    private fun addExpense(groupId: String) {
        val amountStr = amountField.text.toString().trim()
        val description = descriptionField.text.toString().trim()

        if (amountStr.isEmpty()) {
            amountField.error = "Amount is required"
            return
        }

        if (description.isEmpty()) {
            descriptionField.error = "Description is required"
            return
        }

        val amount: Double
        try {
            amount = amountStr.toDouble()
        } catch (e: NumberFormatException) {
            amountField.error = "Invalid amount"
            return
        }

        // Get group currency
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                val currency = document.getString("currency") ?: "USD"
                createExpense(groupId, amount, description, currency)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error getting group data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createExpense(groupId: String, amount: Double, description: String, currency: String) {
        // Create expense in Firestore
        val expenseId = UUID.randomUUID().toString()
        val userId = auth.currentUser?.uid ?: return

        // Get username for paidBy field
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("Username") ?: "I"

                val expense = HashMap<String, Any>()
                expense["amount"] = amount
                expense["description"] = description
                expense["groupName"] = groupId
                expense["paidBy"] = username
                expense["date"] = System.currentTimeMillis()
                expense["currency"] = currency
                expense["imgUrl"] = ""

                if (selectedImageUri != null) {
                    // Show loading state
                    submitButton.isEnabled = false
                    submitButton.text = "Adding..."

                    // Upload image to Cloudinary
                    context?.let { ctx ->
                        CloudinaryHelper.uploadImage(
                            ctx,
                            selectedImageUri!!,
                            "expense_images",
                            onSuccess = { imageUrl ->
                                expense["imgUrl"] = imageUrl
                                saveExpenseToFirestore(expenseId, expense, groupId)
                            },
                            onError = { error ->
                                Toast.makeText(context, "Failed to upload image: $error", Toast.LENGTH_SHORT).show()
                                submitButton.isEnabled = true
                                submitButton.text = "Add Expense"
                            }
                        )
                    }
                } else {
                    saveExpenseToFirestore(expenseId, expense, groupId)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Modify the saveExpenseToFirestore method to pass a flag to scroll to top
    private fun saveExpenseToFirestore(expenseId: String, expense: Map<String, Any>, groupId: String) {
        db.collection("expenses").document(expenseId)
            .set(expense)
            .addOnSuccessListener {
                Toast.makeText(context, "Expense added successfully", Toast.LENGTH_SHORT).show()

                // Create an Expense object to add to the ViewModel
                val newExpense = com.example.androidproject.model.Expense(
                    id = expenseId,
                    amount = expense["amount"] as Double,
                    description = expense["description"] as String,
                    groupName = expense["groupName"] as String,
                    paidBy = expense["paidBy"] as String,
                    date = expense["date"] as Long,
                    imgUrl = expense["imgUrl"] as String,
                    currency = expense["currency"] as String
                )

                // Get the ViewModel and add the expense directly
                val viewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.example.androidproject.viewmodel.GroupExpensesViewModel::class.java]
                viewModel.addExpenseToViewModel(newExpense)

                // Navigate back to GroupExpensesFragment without any extra arguments
                val action = AddExpenseFragmentDirections.actionAddExpenseFragmentToGroupExpensesFragment(groupId)
                findNavController().navigate(action)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
                submitButton.isEnabled = true
                submitButton.text = "Add Expense"
            }
    }
}
