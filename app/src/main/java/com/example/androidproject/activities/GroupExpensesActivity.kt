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

    private lateinit var loadingBar: ProgressBar
    private lateinit var db: FirebaseFirestore
    private var groupName: String = ""
    private var groupCurrency: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_expenses)

        loadingBar = findViewById(R.id.loadingBar)
        loadingBar.visibility = View.VISIBLE

        val expensesRecyclerView = findViewById<RecyclerView>(R.id.expensesRecyclerView)
        val noExpensesTextView = findViewById<TextView>(R.id.noExpensesTextView)
        val locationTextView = findViewById<TextView>(R.id.locationText)

        groupName = intent.getStringExtra("GROUP_NAME")!!
        val newCurrencyName = intent.getStringExtra("CURRENCY_NAME")
        val newCurrencyValue = intent.getStringExtra("CURRENCY_VALUE")?.toDouble()

        locationTextView.text = "Location: $groupName"

        try {
            db = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("GroupExpensesActivity", "Error initializing Firestore", e)
            finish()
            return
        }

        if (newCurrencyValue != null) {
            updateGroupCurrency(groupName, newCurrencyName)
            updateExpensesCurrency(groupName, newCurrencyValue)
            groupCurrency = newCurrencyName!!
        }

        getCurrency()
        getDescription()

        val changeCurrencyButton: TextView = findViewById(R.id.changeCurrency)
        changeCurrencyButton.setOnClickListener {
            val intent = Intent(this, RecycleViewCoinActivity::class.java)
            intent.putExtra("GROUP_NAME", groupName)
            intent.putExtra("CURRENCY_NAME", groupCurrency)
            startActivity(intent)
        }

        val addExpenseButton: TextView = findViewById(R.id.addExpense)
        addExpenseButton.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("GROUP_NAME", groupName)
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

    fun updateExpensesCurrency(groupName: String, newCurrency: Double) {
        db.collection("expenses").whereEqualTo("groupName", groupName).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("expenses").document(document.id).update(
                        "amount",
                        String.format("%.2f", document.getDouble("amount")!!.times(newCurrency))
                            .toDouble()
                    )
                }
            }.addOnFailureListener { e ->
                Log.e("GroupExpensesActivity", "Error updating expenses currency", e)
            }
    }

    fun updateGroupCurrency(groupName: String, newCurrency: String?) {
        db.collection("groups").whereEqualTo("groupName", groupName).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("groups").document(document.id).update("currency", newCurrency)
                }
            }.addOnFailureListener { e ->
                Log.e("GroupExpensesActivity", "Error updating group currency", e)
            }
    }

    fun getCurrency() {
        if (groupCurrency == "") {

            val groupCurrencyTextView = findViewById<TextView>(R.id.groupCurrency)

            db.collection("groups").whereEqualTo("groupName", groupName).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("GroupExpensesActivity", "No documents found for group: $groupName")
                    } else {
                        for (document in documents) {
                            groupCurrency = document.getString("currency")!!
                            groupCurrencyTextView.text = "Currency is: $groupCurrency"
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("GroupExpensesActivity", "Error getting group currency", e)
                }
        } else {
            val groupCurrencyTextView = findViewById<TextView>(R.id.groupCurrency)
            groupCurrencyTextView.text = "Currency is: $groupCurrency"
        }
    }

    fun getDescription(){
        val groupDescriptionTextView = findViewById<TextView>(R.id.groupDescriptionTextView)

        db.collection("groups").whereEqualTo("groupName", groupName).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("GroupExpensesActivity", "No documents found for group: $groupName")
                } else {
                    for (document in documents) {
                        groupDescriptionTextView.text = document.getString("description")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("GroupExpensesActivity", "Error getting group description", e)
            }
    }
}