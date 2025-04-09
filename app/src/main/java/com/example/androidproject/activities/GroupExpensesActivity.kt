package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class GroupExpensesActivity : AppCompatActivity() {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var noExpensesTextView: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var addExpenseButton: CardView
    private lateinit var locationText: TextView
    private lateinit var groupDescriptionTextView: TextView
    private lateinit var groupCurrency: TextView

    private var groupId: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_expenses)

        // Get group ID from intent
        groupId = intent.getStringExtra("groupId")
        if (groupId == null) {
            finish()
            return
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
        noExpensesTextView = findViewById(R.id.noExpensesTextView)
        loadingBar = findViewById(R.id.loadingBar)
        addExpenseButton = findViewById(R.id.cvAddExpense)
        locationText = findViewById(R.id.locationText)
        groupDescriptionTextView = findViewById(R.id.groupDescriptionTextView)
        groupCurrency = findViewById(R.id.groupCurrency)

        // Set up RecyclerView
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up click listener for add expense button
        addExpenseButton.setOnClickListener {
            val intent = Intent(this@GroupExpensesActivity, AddExpenseActivity::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }

        // Load group data and expenses
        loadGroupData()
        loadExpenses()
    }

    private fun loadGroupData() {
        db.collection("groups").document(groupId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        locationText.text = data.getOrDefault("name", "Group").toString()
                        groupDescriptionTextView.text = data.getOrDefault("description", "").toString()
                        groupCurrency.text = "Currency: USD" // Default or from data
                    }
                }
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    private fun loadExpenses() {
        loadingBar.visibility = View.VISIBLE

        db.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .get()
            .addOnCompleteListener { task ->
                loadingBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val expenses = ArrayList<Map<String, Any>>()

                    for (document in task.result!!) {
                        expenses.add(document.data)
                    }

                    if (expenses.isEmpty()) {
                        noExpensesTextView.visibility = View.VISIBLE
                        expensesRecyclerView.visibility = View.GONE
                    } else {
                        noExpensesTextView.visibility = View.GONE
                        expensesRecyclerView.visibility = View.VISIBLE

                        // Set up adapter with expenses data
                        // This would be implemented with your adapter
                    }
                } else {
                    // Handle error
                }
            }
    }

    override fun onResume() {
        super.onResume()
        // Refresh expenses when returning to this activity
        loadExpenses()
    }
}
