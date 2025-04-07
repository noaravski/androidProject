package com.example.androidproject.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.adapters.ExpensesAdapter
import com.example.androidproject.model.Expense
import com.google.firebase.firestore.FirebaseFirestore

class GroupExpensesActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_expenses)

        val expensesRecyclerView = findViewById<RecyclerView>(R.id.expensesRecyclerView)
        val noExpensesTextView = findViewById<TextView>(R.id.noExpensesTextView)

        val groupName = intent.getStringExtra("GROUP_NAME")
        Log.d("GroupExpensesActivity", "Group Name: $groupName")
        val db = FirebaseFirestore.getInstance()
        val expensesCollection = db.collection("expenses")

        expensesCollection.whereEqualTo("GroupName", groupName).get()
            .addOnSuccessListener { documents ->
                val expenses = mutableListOf<Expense>()

                if (documents.isEmpty) {
                    Log.d("GroupExpensesActivity", "No expenses yet")
                    noExpensesTextView.visibility = View.VISIBLE
                } else {
                    for (document in documents) {
                        val expense = Expense(
                            GroupName = document.getString("GroupName"),
                            Description = document.getString("Description"),
                            Amount = document.getDouble("Amount"),
                            Currency = document.getString("Currency"),
                            Date = document.getTimestamp("Date"),
                            ImgUrl = document.getString("ImgUrl"),
                            PaidBy = document.getString("PaidBy"),
                            SplitBetween = document.get("SplitBetween") as List<String>?
                        )
                        expenses.add(expense)
                        val adapter = ExpensesAdapter(expenses)
                        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
                        expensesRecyclerView.adapter = adapter
                    }

                }
            }.addOnFailureListener { exception ->
                Log.e("GroupExpensesActivity", "Error getting documents: ", exception)
            }
    }
}