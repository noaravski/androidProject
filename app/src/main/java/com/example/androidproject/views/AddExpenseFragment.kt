package com.example.androidproject.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddExpenseFragment : Fragment() {

    private lateinit var amountField: EditText
    private lateinit var descriptionField: EditText
    private lateinit var uploadButton: ImageButton
    private lateinit var submitButton: Button
    private lateinit var loadingBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private var groupId: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            uploadButton.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)

        // Get group ID from arguments
        groupId = arguments?.getString("groupId")
        if (groupId == null) {
            activity?.finish()
            return null
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        amountField = view.findViewById(R.id.amountField)
        descriptionField = view.findViewById(R.id.descriptionField)
        uploadButton = view.findViewById(R.id.uploadButton)
        submitButton = view.findViewById(R.id.submitButton)
        loadingBar = view.findViewById(R.id.loadingBar)

        // Set up click listeners
        uploadButton.setOnClickListener { getContent.launch("image/*") }
        submitButton.setOnClickListener { addExpense() }

        return view
    }

    private fun addExpense() {
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

        // Show loading
        loadingBar.visibility = View.VISIBLE

        // Create expense in Firestore
        val expenseId = UUID.randomUUID().toString()
        val userId = auth.currentUser?.uid ?: return

        val expense = HashMap<String, Any>()
        expense["amount"] = amount
        expense["description"] = description
        expense["groupId"] = groupId!!
        expense["createdBy"] = userId
        expense["createdAt"] = System.currentTimeMillis()

        if (selectedImageUri != null) {
            // Upload image to Firebase Storage
            val storageRef = storage.reference.child("expense_images/$expenseId")
            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        expense["imageUrl"] = uri.toString()
                        saveExpenseToFirestore(expenseId, expense)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    saveExpenseToFirestore(expenseId, expense)
                }
        } else {
            saveExpenseToFirestore(expenseId, expense)
        }
    }

    private fun saveExpenseToFirestore(expenseId: String, expense: Map<String, Any>) {
        db.collection("expenses").document(expenseId)
            .set(expense)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to the previous fragment
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                // Hide loading
                loadingBar.visibility = View.GONE
            }
    }

    companion object {
        fun newInstance(groupId: String): AddExpenseFragment {
            val fragment = AddExpenseFragment()
            val args = Bundle()
            args.putString("groupId", groupId)
            fragment.arguments = args
            return fragment
        }
    }
}