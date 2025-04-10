package com.example.androidproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.adapters.ExpensesAdapter
import com.example.androidproject.model.Expense
import com.example.androidproject.model.Group
import com.example.androidproject.utils.ProfileImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import kotlin.math.max

class GroupExpensesFragment : Fragment() {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var noExpensesTextView: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var addExpenseButton: CardView
    private lateinit var changeCurrencyButton: CardView
    private lateinit var locationText: TextView
    private lateinit var groupDescriptionTextView: TextView
    private lateinit var groupCurrency: TextView
    private lateinit var groupMembersCount: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var currencySymbolText: TextView
    private lateinit var expenseSummaryText: TextView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var expensesAdapter: ExpensesAdapter
    private val expensesList = mutableListOf<Expense>()

    private var currentGroup: Group? = null
    private var exchangeRates: Map<String, Double>? = null
    private var expensesListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get group ID from arguments
        val args: GroupExpensesFragmentArgs by navArgs()
        val groupId = args.groupId

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView)
        noExpensesTextView = view.findViewById(R.id.noExpensesTextView)
        loadingBar = view.findViewById(R.id.loadingBar)
        addExpenseButton = view.findViewById(R.id.cvAddExpense)
        changeCurrencyButton = view.findViewById(R.id.cvDollarIcon)
        locationText = view.findViewById(R.id.locationText)
        groupDescriptionTextView = view.findViewById(R.id.groupDescriptionTextView)
        groupCurrency = view.findViewById(R.id.groupCurrency)
        groupMembersCount = view.findViewById(R.id.groupMembersCount)
        profileImage = view.findViewById(R.id.profileImage)
        currencySymbolText = view.findViewById(R.id.changeCurrency)
        expenseSummaryText = view.findViewById(R.id.expenseSummaryText)

        // Set up RecyclerView
        expensesRecyclerView.layoutManager = LinearLayoutManager(context)
        expensesAdapter = ExpensesAdapter(expensesList)
        expensesRecyclerView.adapter = expensesAdapter

        // Set up click listener for add expense button
        addExpenseButton.setOnClickListener {
            val action = GroupExpensesFragmentDirections.actionGroupExpensesFragmentToAddExpenseFragment(groupId)
            findNavController().navigate(action)
        }

        // Set up click listener for change currency button
        changeCurrencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }

        // Load exchange rates
        fetchExchangeRates()

        // Load group data and expenses
        loadGroupData(groupId)
        loadExpenses(groupId)
    }

    private fun loadGroupData(groupId: String) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        val members = data.getOrDefault("members", listOf<String>()) as? List<String> ?: listOf()

                        // Handle createdBy as String
                        val createdBy = when (val createdByValue = data["createdBy"]) {
                            is String -> createdByValue
                            else -> ""
                        }

                        currentGroup = Group(
                            id = documentSnapshot.id,
                            groupName = data.getOrDefault("groupName", "Group").toString(),
                            description = data.getOrDefault("description", "").toString(),
                            createdBy = createdBy,
                            createdAt = data.getOrDefault("createdAt", 0L) as Long,
                            imageUrl = data.getOrDefault("imageUrl", null) as? String,
                            currency = data.getOrDefault("currency", "USD").toString(),
                            members = members
                        )

                        // Update UI with group data
                        locationText.text = currentGroup?.groupName
                        groupDescriptionTextView.text = currentGroup?.description
                        groupCurrency.text = "Currency: ${currentGroup?.currency}"
                        groupMembersCount.text = "Members: ${members.size}"
                        currencySymbolText.text = getCurrencySymbol(currentGroup?.currency ?: "USD")

                        // Load group image from Firestore
                        context?.let { ctx ->
                            if (!currentGroup?.imageUrl.isNullOrEmpty() && currentGroup?.imageUrl != "default") {
                                // Use the imageUrl field for groups
                                ProfileImageLoader.loadGroupImage(ctx, groupId, profileImage)
                            } else {
                                profileImage.setImageResource(R.drawable.island)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    private fun loadExpenses(groupId: String) {
        loadingBar.visibility = View.VISIBLE

        // Remove previous listener if exists
        expensesListener?.remove()

        // Set up real-time listener for expenses
        expensesListener = db.collection("expenses")
            .whereEqualTo("groupName", groupId)
            .addSnapshotListener { snapshot, error ->
                loadingBar.visibility = View.GONE

                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    expensesList.clear()

                    for (document in snapshot.documents) {
                        val expense = Expense(
                            id = document.id,
                            amount = document.getDouble("amount") ?: 0.0,
                            description = document.getString("description") ?: "",
                            groupName = document.getString("groupName") ?: "",
                            paidBy = document.getString("paidBy") ?: "",
                            date = document.getLong("date") ?: 0,
                            imgUrl = document.getString("imgUrl"),
                            currency = document.getString("currency") ?: "USD"
                        )
                        expensesList.add(expense)
                    }

                    if (expensesList.isEmpty()) {
                        noExpensesTextView.visibility = View.VISIBLE
                        expensesRecyclerView.visibility = View.GONE
                        expenseSummaryText.visibility = View.GONE
                    } else {
                        noExpensesTextView.visibility = View.GONE
                        expensesRecyclerView.visibility = View.VISIBLE
                        expenseSummaryText.visibility = View.VISIBLE
                        expensesAdapter.notifyDataSetChanged()

                        // Calculate and display expense summary
                        calculateExpenseSummary()
                    }
                }
            }
    }

    private fun calculateExpenseSummary() {
        if (expensesList.isEmpty()) {
            expenseSummaryText.visibility = View.GONE
            return
        }

        expenseSummaryText.visibility = View.VISIBLE

        val totalAmount = expensesList.sumOf { it.amount }
        val currency = currentGroup?.currency ?: "USD"
        val currencySymbol = getCurrencySymbol(currency)

        // Map to track how much each person paid
        val paidByMap = mutableMapOf<String, Double>()

        // Calculate how much each person paid
        for (expense in expensesList) {
            val paidBy = expense.paidBy
            val amount = expense.amount
            paidByMap[paidBy] = (paidByMap[paidBy] ?: 0.0) + amount
        }

        // Calculate average amount per person
        val memberCount = max(currentGroup?.members?.size ?: 1, 1) // Ensure we don't divide by zero
        val averagePerPerson = totalAmount / memberCount

        // Build summary text
        val summaryBuilder = StringBuilder()
        summaryBuilder.append("Total spent: $currencySymbol${String.format("%.2f", totalAmount)}\n\n")

        // Add individual payments
        summaryBuilder.append("Payments:\n")
        for ((person, paid) in paidByMap) {
            summaryBuilder.append("$person paid $currencySymbol${String.format("%.2f", paid)}\n")
        }
        summaryBuilder.append("\n")

        // Calculate who owes whom
        if (paidByMap.size > 1) {
            summaryBuilder.append("Settlements:\n")

            // Sort by amount paid (ascending)
            val sortedPayers = paidByMap.entries.sortedBy { it.value }

            // Find who paid less than average (debtors) and who paid more (creditors)
            val debtors = sortedPayers.filter { it.value < averagePerPerson }
            val creditors = sortedPayers.filter { it.value > averagePerPerson }

            // For each debtor, calculate how much they owe to creditors
            for (debtor in debtors) {
                var debtRemaining = averagePerPerson - debtor.value

                for (creditor in creditors) {
                    val creditRemaining = creditor.value - averagePerPerson

                    if (creditRemaining > 0 && debtRemaining > 0) {
                        val transferAmount = minOf(debtRemaining, creditRemaining)
                        if (transferAmount > 0.01) { // Threshold to avoid tiny amounts
                            summaryBuilder.append("${debtor.key} owes ${creditor.key} $currencySymbol${String.format("%.2f", transferAmount)}\n")
                            debtRemaining -= transferAmount
                        }
                    }

                    if (debtRemaining <= 0.01) break
                }
            }
        }

        // Set the summary text
        expenseSummaryText.text = summaryBuilder.toString()
    }

    private fun fetchExchangeRates() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.exchangerate-api.com/v4/latest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val exchangeRateService = retrofit.create(ExchangeRateService::class.java)

        exchangeRateService.getExchangeRates("USD").enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful) {
                    exchangeRates = response.body()?.rates
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                // Handle error
            }
        })
    }

    private fun showCurrencySelectionDialog() {
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "ILS")

        context?.let { ctx ->
            android.app.AlertDialog.Builder(ctx)
                .setTitle("Select Currency")
                .setItems(currencies) { _, which ->
                    val selectedCurrency = currencies[which]
                    updateGroupCurrency(selectedCurrency)
                }
                .show()
        }
    }

    private fun updateGroupCurrency(newCurrency: String) {
        if (currentGroup?.currency == newCurrency) return

        val oldCurrency = currentGroup?.currency ?: "USD"
        val groupId = currentGroup?.id ?: return

        // Update group in Firestore
        db.collection("groups").document(groupId)
            .update("currency", newCurrency)
            .addOnSuccessListener {
                // Update UI
                currentGroup?.currency = newCurrency
                groupCurrency.text = "Currency: $newCurrency"
                currencySymbolText.text = getCurrencySymbol(newCurrency)

                // Convert all expenses to new currency
                convertExpensesToNewCurrency(oldCurrency, newCurrency)

                Toast.makeText(context, "Currency updated to $newCurrency", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating currency: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertExpensesToNewCurrency(oldCurrency: String, newCurrency: String) {
        if (exchangeRates == null) {
            Toast.makeText(context, "Exchange rates not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Get exchange rates
        val oldRate = exchangeRates!![oldCurrency] ?: 1.0
        val newRate = exchangeRates!![newCurrency] ?: 1.0

        // Calculate conversion factor
        val conversionFactor = newRate / oldRate

        // Update expenses in Firestore
        for (expense in expensesList) {
            val newAmount = expense.amount * conversionFactor

            db.collection("expenses").document(expense.id)
                .update(
                    mapOf(
                        "amount" to newAmount,
                        "currency" to newCurrency
                    )
                )
                .addOnSuccessListener {
                    // Update local expense
                    expense.amount = newAmount
                }
        }

        // Refresh adapter
        expensesAdapter.notifyDataSetChanged()

        // Recalculate expense summary
        calculateExpenseSummary()
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CAD" -> "C$"
            "AUD" -> "A$"
            "ILS" -> "₪"
            else -> currencyCode
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener
        expensesListener?.remove()
    }

    // API Service interface
    interface ExchangeRateService {
        @GET("{base}")
        fun getExchangeRates(@Path("base") base: String): Call<ExchangeRateResponse>
    }

    // Response data class
    data class ExchangeRateResponse(
        val base: String,
        val date: String,
        val rates: Map<String, Double>
    )
}
