package com.example.androidproject.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.HashMap
import java.util.UUID

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var amountField: EditText
    private lateinit var descriptionField: EditText
    private lateinit var uploadButton: ImageButton
    private lateinit var submitButton: Button

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Get group ID from intent
        groupId = intent.getStringExtra("groupId")
        if (groupId == null) {
            finish()
            return
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        amountField = findViewById(R.id.amountField)
        descriptionField = findViewById(R.id.descriptionField)
        uploadButton = findViewById(R.id.uploadButton)
        submitButton = findViewById(R.id.submitButton)

        // Set up click listeners
        uploadButton.setOnClickListener { getContent.launch("image/*") }
        submitButton.setOnClickListener { addExpense() }
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
                    Toast.makeText(this@AddExpenseActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@AddExpenseActivity, "Expense added successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to GroupExpensesActivity
                val intent = Intent(this@AddExpenseActivity, GroupExpensesActivity::class.java)
                intent.putExtra("groupId", groupId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@AddExpenseActivity, "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
