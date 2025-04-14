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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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
    private lateinit var editGroupButton: CardView
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
        editGroupButton = view.findViewById(R.id.cvEditGroup)
        locationText = view.findViewById(R.id.locationText)
        groupDescriptionTextView = view.findViewById(R.id.groupDescriptionTextView)
        groupCurrency = view.findViewById(R.id.groupCurrency)
        groupMembersCount = view.findViewById(R.id.groupMembersCount)
        profileImage = view.findViewById(R.id.groupProfileImage)
        currencySymbolText = view.findViewById(R.id.changeCurrency)
        expenseSummaryText = view.findViewById(R.id.expenseSummaryText)

        // Set up RecyclerView with padding to account for bottom navigation
        val layoutManager = LinearLayoutManager(context)
        expensesRecyclerView.layoutManager = layoutManager

        // Add bottom padding to RecyclerView to prevent last item from being hidden
        expensesRecyclerView.setPadding(
            expensesRecyclerView.paddingLeft,
            expensesRecyclerView.paddingTop,
            expensesRecyclerView.paddingRight,
            resources.getDimensionPixelSize(R.dimen.bottom_nav_height) // Use a dimension resource or hardcode the height (e.g., 56dp)
        )
        expensesRecyclerView.clipToPadding = false

        expensesAdapter = ExpensesAdapter(expensesList)
        expensesAdapter.setOnItemClickListener { expense ->
            // Navigate to edit expense fragment
            val action =
                GroupExpensesFragmentDirections.actionGroupExpensesFragmentToEditExpenseFragment(
                    expense.id
                )
            findNavController().navigate(action)
        }
        expensesRecyclerView.adapter = expensesAdapter

        // Set up click listener for add expense button
        addExpenseButton.setOnClickListener {
            val action =
                GroupExpensesFragmentDirections.actionGroupExpensesFragmentToAddExpenseFragment(
                    groupId
                )
            findNavController().navigate(action)
        }

        // Set up click listener for change currency button
        changeCurrencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }

        // Set up click listener for edit group button
        editGroupButton.setOnClickListener {
            val action =
                GroupExpensesFragmentDirections.actionGroupExpensesFragmentToEditGroupFragment(
                    groupId
                )
            findNavController().navigate(action)
        }

        // Load exchange rates
        fetchExchangeRates()

        // Load group data and expenses
        loadGroupData(groupId)
        loadExpenses(groupId)
    }

    // Fix the loadGroupData method to properly load group images
    private fun loadGroupData(groupId: String) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        val members =
                            data.getOrDefault("members", listOf<String>()) as? List<String>
                                ?: listOf()

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
                            val imageUrl = currentGroup?.imageUrl
                            if (!imageUrl.isNullOrEmpty() && imageUrl != "default") {
                                // Load the image directly using Glide
                                com.bumptech.glide.Glide.with(ctx)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.island)
                                    .error(R.drawable.island)
                                    .into(profileImage)
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

        // Set up real-time listener for expenses, ordered by date (newest first)
        expensesListener = db.collection("expenses")
            .whereEqualTo("groupName", groupId)
            .orderBy("date", Query.Direction.DESCENDING) // Sort by date, newest first
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
        summaryBuilder.append(
            "Total spent: $currencySymbol${
                String.format(
                    "%.2f",
                    totalAmount
                )
            }\n\n"
        )

        // Add individual payments
        summaryBuilder.append("Payments:\n")
        for ((person, paid) in paidByMap) {
            summaryBuilder.append("$person paid $currencySymbol${String.format("%.2f", paid)}\n")
        }
        summaryBuilder.append("\n")

        // Calculate who owes whom - always show this section even if there's only one person
        summaryBuilder.append("Settlements:\n")

        if (paidByMap.size <= 1 || memberCount <= 1) {
            // Handle edge case of only one person
            summaryBuilder.append("No settlements needed with only one person.\n")
        } else {
            // Create a list of people who paid less than average (debtors)
            val debtors = mutableListOf<Pair<String, Double>>()
            // Create a list of people who paid more than average (creditors)
            val creditors = mutableListOf<Pair<String, Double>>()

            for ((person, paid) in paidByMap) {
                val diff = paid - averagePerPerson
                if (diff < -0.01) { // Debtor
                    debtors.add(Pair(person, -diff)) // Store how much they owe
                } else if (diff > 0.01) { // Creditor
                    creditors.add(Pair(person, diff)) // Store how much they're owed
                }
            }

            // Sort debtors by amount owed (descending)
            debtors.sortByDescending { it.second }
            // Sort creditors by amount owed (descending)
            creditors.sortByDescending { it.second }

            // For each debtor, find creditors to pay
            if (debtors.isEmpty() || creditors.isEmpty()) {
                summaryBuilder.append("Everyone paid equally.\n")
            } else {
                for ((debtor, debtAmount) in debtors) {
                    var remainingDebt = debtAmount

                    for (i in creditors.indices) {
                        val (creditor, creditAmount) = creditors[i]

                        if (creditAmount > 0 && remainingDebt > 0) {
                            // Calculate how much this debtor pays this creditor
                            val payment = minOf(remainingDebt, creditAmount)

                            if (payment > 0.01) { // Only show meaningful payments
                                summaryBuilder.append(
                                    "$debtor owes $creditor $currencySymbol${
                                        String.format(
                                            "%.2f",
                                            payment
                                        )
                                    }\n"
                                )

                                // Update remaining amounts
                                remainingDebt -= payment
                                creditors[i] = Pair(creditor, creditAmount - payment)
                            }
                        }

                        if (remainingDebt <= 0.01) break
                    }
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

        exchangeRateService.getExchangeRates("USD")
            .enqueue(object : Callback<ExchangeRateResponse> {
                override fun onResponse(
                    call: Call<ExchangeRateResponse>,
                    response: Response<ExchangeRateResponse>
                ) {
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

                Toast.makeText(context, "Currency updated to $newCurrency", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating currency: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
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
