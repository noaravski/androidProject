package com.example.androidproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.example.androidproject.viewmodel.GroupExpensesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.androidproject.utils.ProfileImageLoader
import com.example.androidproject.utils.CurrencyConverter
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.max

class GroupExpensesFragment : Fragment() {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var noExpensesTextView: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var addExpenseButton: TextView
    private lateinit var changeCurrencyButton: TextView
    private lateinit var editGroupButton: ImageView
    private lateinit var filterButton: ImageView
    private lateinit var locationText: TextView
    private lateinit var groupDescriptionTextView: TextView
    private lateinit var groupCurrency: TextView
    private lateinit var groupMembersCount: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var expenseSummaryText: TextView

    private lateinit var viewModel: GroupExpensesViewModel
    private lateinit var expensesAdapter: ExpensesAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[GroupExpensesViewModel::class.java]

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView)
        noExpensesTextView = view.findViewById(R.id.noExpensesTextView)
        loadingBar = view.findViewById(R.id.loadingBar)
        addExpenseButton = view.findViewById(R.id.addExpense)
        changeCurrencyButton = view.findViewById(R.id.changeCurrency)
        editGroupButton = view.findViewById(R.id.editGroupIcon)
        filterButton = view.findViewById(R.id.filterIcon)
        locationText = view.findViewById(R.id.locationText)
        groupDescriptionTextView = view.findViewById(R.id.groupDescriptionTextView)
        groupCurrency = view.findViewById(R.id.groupCurrency)
        groupMembersCount = view.findViewById(R.id.groupMembersCount)
        profileImage = view.findViewById(R.id.groupProfileImage)
        expenseSummaryText = view.findViewById(R.id.expenseSummaryText)

        // Set up RecyclerView with padding to account for bottom navigation
        val layoutManager = LinearLayoutManager(context)
        expensesRecyclerView.layoutManager = layoutManager

        // Add bottom padding to RecyclerView to prevent last item from being hidden
        expensesRecyclerView.setPadding(
            expensesRecyclerView.paddingLeft,
            expensesRecyclerView.paddingTop,
            expensesRecyclerView.paddingRight,
            resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        )
        expensesRecyclerView.clipToPadding = false

        // Initialize adapter with empty list and set click listener
        expensesAdapter = ExpensesAdapter(emptyList())
        expensesAdapter.setOnItemClickListener { expense ->
            val action = GroupExpensesFragmentDirections
                .actionGroupExpensesFragmentToEditExpenseFragment(expense.id)
            findNavController().navigate(action)
        }
        expensesRecyclerView.adapter = expensesAdapter

        // Set up click listeners
        addExpenseButton.setOnClickListener {
            val action = GroupExpensesFragmentDirections.actionGroupExpensesFragmentToAddExpenseFragment(groupId)
            findNavController().navigate(action)
        }

        changeCurrencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }

        editGroupButton.setOnClickListener {
            val action = GroupExpensesFragmentDirections.actionGroupExpensesFragmentToEditGroupFragment(groupId)
            findNavController().navigate(action)
        }

        filterButton.setOnClickListener {
            viewModel.toggleExpenseFilter()
        }

        // Set up observers
        setupObservers(groupId)

        // Load data - ensure we load group first, then expenses
        viewModel.loadGroup(groupId)
        viewModel.loadExpenses(groupId)

        // Explicitly set the initial filter state to show all expenses
        viewModel.setInitialFilterState()
    }

    private fun setupObservers(groupId: String) {
        // Observe group data
        viewModel.group.observe(viewLifecycleOwner) { group ->
            group?.let {
                locationText.text = it.groupName
                groupDescriptionTextView.text = it.description
                groupCurrency.text = "Currency: ${it.currency}"
                groupMembersCount.text = "Members: ${it.members.size}"
                changeCurrencyButton.text = getCurrencySymbol(it.currency)

                // Load group image
                context?.let { ctx ->
                    val imageUrl = it.imageUrl
                    if (!imageUrl.isNullOrEmpty() && imageUrl != "default") {
                        Glide.with(ctx)
                            .load(ProfileImageLoader.convertToHttps(imageUrl))
                            .placeholder(R.drawable.island)
                            .error(R.drawable.island)
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.island)
                    }
                }
            }
        }

        // Observe filtered expenses
        viewModel.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            // Debug log to check if we're getting expenses
            android.util.Log.d("GroupExpensesFragment", "Received ${expenses?.size ?: 0} filtered expenses")

            if (expenses.isNullOrEmpty()) {
                noExpensesTextView.visibility = View.VISIBLE
                expensesRecyclerView.visibility = View.GONE
                expenseSummaryText.visibility = View.GONE
            } else {
                noExpensesTextView.visibility = View.GONE
                expensesRecyclerView.visibility = View.VISIBLE
                expenseSummaryText.visibility = View.VISIBLE

                // Update adapter with the new expenses
                expensesAdapter.updateExpenses(expenses)

                // Calculate and display expense summary
                calculateExpenseSummary(expenses)
            }
        }

        // Also observe the non-filtered expenses as a fallback
        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            android.util.Log.d("GroupExpensesFragment", "Raw expenses count: ${expenses?.size ?: 0}")

            // If we have expenses but filteredExpenses is empty, it might be a filter issue
            if (!expenses.isNullOrEmpty() && viewModel.filteredExpenses.value.isNullOrEmpty()) {
                // Force the ViewModel to apply the initial filter state
                viewModel.setInitialFilterState()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe filter state
        viewModel.showOnlyMyExpenses.observe(viewLifecycleOwner) { showOnlyMine ->
            filterButton.setImageResource(
                if (showOnlyMine) R.drawable.ic_person else R.drawable.ic_group
            )
        }
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
        val group = viewModel.group.value ?: return
        if (group.currency == newCurrency) return

        val oldCurrency = group.currency
        val groupId = group.id

        // Disable UI elements during update
        changeCurrencyButton.isEnabled = false
        loadingBar.visibility = View.VISIBLE

        // Update group in Firestore
        db.collection("groups").document(groupId)
            .update("currency", newCurrency)
            .addOnSuccessListener {
                // Update UI
                groupCurrency.text = "Currency: $newCurrency"
                changeCurrencyButton.text = getCurrencySymbol(newCurrency)

                // Update the group in the ViewModel
                val updatedGroup = group.copy(currency = newCurrency)
                viewModel.updateGroupInViewModel(updatedGroup)

                // Convert all expenses to new currency
                convertExpensesToNewCurrency(oldCurrency, newCurrency)
            }
            .addOnFailureListener { e ->
                // Re-enable UI elements
                changeCurrencyButton.isEnabled = true
                loadingBar.visibility = View.GONE

                Toast.makeText(context, "Error updating currency: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertExpensesToNewCurrency(oldCurrency: String, newCurrency: String) {
        val groupId = viewModel.group.value?.id ?: return

        // Show loading indicator
        loadingBar.visibility = View.VISIBLE

        // Use CurrencyConverter to get exchange rate
        CurrencyConverter.getExchangeRate(oldCurrency, newCurrency,
            onSuccess = { rate ->
                // Update expenses with the new currency and converted amounts
                db.collection("expenses")
                    .whereEqualTo("groupName", groupId)
                    .get()
                    .addOnSuccessListener { documents ->
                        val batch = db.batch()

                        for (document in documents) {
                            val expenseRef = db.collection("expenses").document(document.id)
                            val currentAmount = document.getDouble("amount") ?: 0.0
                            val newAmount = currentAmount * rate

                            batch.update(expenseRef, "currency", newCurrency)
                            batch.update(expenseRef, "amount", newAmount)
                        }

                        batch.commit().addOnSuccessListener {
                            // Refresh expenses after batch update
                            viewModel.loadExpenses(groupId)
                            loadingBar.visibility = View.GONE
                            changeCurrencyButton.isEnabled = true

                            Toast.makeText(context,
                                "Currency updated to $newCurrency with rate: $rate",
                                Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { e ->
                            loadingBar.visibility = View.GONE
                            changeCurrencyButton.isEnabled = true
                            Toast.makeText(context,
                                "Error updating expenses: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        loadingBar.visibility = View.GONE
                        changeCurrencyButton.isEnabled = true
                        Toast.makeText(context,
                            "Error fetching expenses: ${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }
            },
            onError = { error ->
                loadingBar.visibility = View.GONE
                changeCurrencyButton.isEnabled = true
                Toast.makeText(context,
                    "Error getting exchange rate: $error",
                    Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun calculateExpenseSummary(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            expenseSummaryText.visibility = View.GONE
            return
        }

        expenseSummaryText.visibility = View.VISIBLE

        val totalAmount = expenses.sumOf { it.amount }
        val group = viewModel.group.value
        val currency = group?.currency ?: "USD"
        val currencySymbol = getCurrencySymbol(currency)

        // Map to track how much each person paid
        val paidByMap = mutableMapOf<String, Double>()

        // Calculate how much each person paid
        for (expense in expenses) {
            val paidBy = expense.paidBy
            val amount = expense.amount
            paidByMap[paidBy] = (paidByMap[paidBy] ?: 0.0) + amount
        }

        // Calculate average amount per person
        val memberCount = max(group?.members?.size ?: 1, 1) // Ensure we don't divide by zero
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
    }

    override fun onResume() {
        super.onResume()

        // Get group ID from arguments
        val args: GroupExpensesFragmentArgs by navArgs()
        val groupId = args.groupId

        // Refresh data when returning to this fragment
        viewModel.loadGroup(groupId)
        viewModel.loadExpenses(groupId)
    }
}
