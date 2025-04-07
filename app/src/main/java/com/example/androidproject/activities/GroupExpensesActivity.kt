package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.adapters.ExpensesAdapter
import com.example.androidproject.model.Expense
import com.google.firebase.firestore.FirebaseFirestore

class GroupExpensesActivity : AppCompatActivity() {

    private var groupCurrency: String? = null
    private lateinit var loadingBar: ProgressBar
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_expenses)

        loadingBar = findViewById(R.id.loadingBar)
        loadingBar.visibility = View.VISIBLE

        val expensesRecyclerView = findViewById<RecyclerView>(R.id.expensesRecyclerView)
        val noExpensesTextView = findViewById<TextView>(R.id.noExpensesTextView)

        val changeCurrencyButton: TextView = findViewById(R.id.changeCurrency)
        changeCurrencyButton.setOnClickListener {
            val intent = Intent(this, RecycleViewCoinActivity::class.java)
            startActivity(intent)
        }

        val groupName = intent.getStringExtra("GROUP_NAME")

        try {
            db = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("GroupExpensesActivity", "Error initializing Firestore", e)
            finish()
            return
        }

        db.collection("groups").whereEqualTo("groupName", groupName!!).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    groupCurrency = document.documents[0].getString("currency")
                }
            }.addOnFailureListener { e ->
            Log.e("GroupExpensesActivity", "Error getting group currency", e)
        }

        val addExpenseButton: TextView = findViewById(R.id.addExpense)
        addExpenseButton.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("GROUP_NAME", groupName)
            intent.putExtra("CURRENCY", groupCurrency)
            startActivity(intent)
        }


        val expensesCollection = db.collection("expenses")

        expensesCollection.whereEqualTo("groupName", groupName)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("GroupExpensesActivity", "Error getting documents: ", e)
                    return@addSnapshotListener
                }

                val expenses = mutableListOf<Expense>()
                if (snapshots != null && !snapshots.isEmpty) {
                    for (document in snapshots.documents) {
                        val expense = Expense(
                            groupName = document.getString("groupName"),
                            description = document.getString("description"),
                            amount = document.getDouble("amount"),
                            currency = document.getString("currency"),
                            date = document.getTimestamp("date"),
                            imgUrl = document.getString("imgUrl"),
                            paidBy = document.getString("paidBy"),
                        )
                        expenses.add(expense)
                    }
                    noExpensesTextView.visibility = View.GONE
                } else {
                    Log.d("GroupExpensesActivity", "No expenses yet")
                    noExpensesTextView.visibility = View.VISIBLE
                }

                expenses.sortByDescending { it.date }

                val adapter = ExpensesAdapter(expenses)
                expensesRecyclerView.layoutManager = LinearLayoutManager(this)
                expensesRecyclerView.adapter = adapter
                loadingBar.visibility = View.GONE
            }
    }
}