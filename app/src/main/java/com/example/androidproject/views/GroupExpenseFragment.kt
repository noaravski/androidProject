package com.example.androidproject.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupExpenseFragment : Fragment() {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var noExpensesTextView: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var addExpenseButton: CardView
    private lateinit var locationText: TextView
    private lateinit var groupDescriptionTextView: TextView
    private lateinit var groupCurrency: TextView

    private var groupName: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group_expenses, container, false)

        // Get group ID from arguments
        groupName = arguments?.getString("groupName")
        if (groupName == null) {
            activity?.finish()
            return null
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView)
        noExpensesTextView = view.findViewById(R.id.noExpensesTextView)
        loadingBar = view.findViewById(R.id.loadingBar)
        addExpenseButton = view.findViewById(R.id.cvAddExpense)
        locationText = view.findViewById(R.id.locationText)
        groupDescriptionTextView = view.findViewById(R.id.groupDescriptionTextView)
        groupCurrency = view.findViewById(R.id.groupCurrency)

        // Set up RecyclerView
        expensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up click listener for add expense button
        addExpenseButton.setOnClickListener {
            val fragment = AddExpenseFragment.newInstance(groupName!!)
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null).commit()
        }

        // Load group data and expenses
        loadGroupData()
        loadExpenses()

        return view
    }

    private fun loadGroupData() {
        db.collection("groups").document(groupName!!).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        locationText.text = data.getOrDefault("name", "Group").toString()
                        groupDescriptionTextView.text =
                            data.getOrDefault("description", "").toString()
                        groupCurrency.text = "Currency: USD" // Default or from data
                    }
                }
            }.addOnFailureListener {
                // Handle error
            }
    }

    private fun loadExpenses() {
        loadingBar.visibility = View.VISIBLE

        db.collection("expenses").whereEqualTo("groupName", groupName).get()
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
        // Refresh expenses when returning to this fragment
        loadExpenses()
    }

    companion object {
        fun newInstance(groupName: String): GroupExpenseFragment {
            val fragment = GroupExpenseFragment()
            val args = Bundle()
            args.putString("groupName", groupName)
            fragment.arguments = args
            return fragment
        }
    }
}