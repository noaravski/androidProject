package com.example.androidproject.activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.R
import com.example.androidproject.model.Expense
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AddExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val submitButton: Button = findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val descriptionField: EditText = findViewById(R.id.descriptionField)
            val amountField: EditText = findViewById(R.id.amountField)

            val description = descriptionField.text.toString()
            val amount = amountField.text.toString().toDoubleOrNull()

            if (description.isNotEmpty() && amount != null) {
                val expense = Expense(
                    groupName = intent.getStringExtra("GROUP_NAME"),
                    description = description,
                    amount = amount,
                    currency = intent.getStringExtra("CURRENCY"),
                    date = Timestamp.now(),
                    imgUrl = "https://s",
                    paidBy = "Me",
                )

                val db = FirebaseFirestore.getInstance()
                db.collection("expenses")
                    .add(expense)
                    .addOnSuccessListener {
                        Log.d("AddExpenseActivity", "Expense added successfully")
                        finish()
                        val intent = Intent(this, GroupExpensesActivity::class.java)
                        intent.putExtra("GROUP_NAME", intent.getStringExtra("GROUP_NAME"))
                        intent.putExtra("CURRENCY", intent.getStringExtra("CURRENCY"))
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Log.e("AddExpenseActivity", "Error adding expense", e)
                    }
            } else {
                Log.e("AddExpenseActivity", "Description or amount is invalid")
            }
        }


    }
}