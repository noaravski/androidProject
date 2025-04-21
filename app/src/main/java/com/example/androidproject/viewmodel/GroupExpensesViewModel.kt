package com.example.androidproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.repository.ExpenseRepository
import com.example.androidproject.data.repository.GroupRepository
import com.example.androidproject.model.Expense
import com.example.androidproject.model.Group
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class GroupExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val expenseRepository = ExpenseRepository(application)
    private val groupRepository = GroupRepository(application)
    private val auth = FirebaseAuth.getInstance()

    // LiveData for expenses
    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    // LiveData for filtered expenses
    private val _filteredExpenses = MutableLiveData<List<Expense>>()
    val filteredExpenses: LiveData<List<Expense>> = _filteredExpenses

    // LiveData for current group
    private val _group = MutableLiveData<Group>()
    val group: LiveData<Group> = _group

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Filter state
    private val _showOnlyMyExpenses = MutableLiveData<Boolean>(false)
    val showOnlyMyExpenses: LiveData<Boolean> = _showOnlyMyExpenses

    // Remove the shouldScrollToTop flag
    //var shouldScrollToTop = false

    // Load expenses for a group
    fun loadExpenses(groupId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // First, try to get expenses from Firestore directly for immediate update
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("expenses")
                    .whereEqualTo("groupName", groupId)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val expensesList = snapshot.documents.map { document ->
                    Expense(
                        id = document.id,
                        amount = document.getDouble("amount") ?: 0.0,
                        description = document.getString("description") ?: "",
                        groupName = document.getString("groupName") ?: "",
                        paidBy = document.getString("paidBy") ?: "",
                        date = document.getLong("date") ?: 0,
                        imgUrl = document.getString("imgUrl"),
                        currency = document.getString("currency") ?: "USD"
                    )
                }

                // Update the expenses LiveData immediately
                _expenses.value = expensesList

                // Apply filter
                applyFilter()

                // Then, set up the LiveData from the repository for future updates
                val expensesLiveData = expenseRepository.getExpensesByGroup(groupId)
                expensesLiveData.observeForever { repoExpensesList ->
                    _expenses.value = repoExpensesList
                    applyFilter()
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error loading expenses: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Load group data
    fun loadGroup(groupId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val groupLiveData = groupRepository.getGroupById(groupId)
                groupLiveData.observeForever { group ->
                    _group.value = group
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading group: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Toggle expense filter
    fun toggleExpenseFilter() {
        _showOnlyMyExpenses.value = !(_showOnlyMyExpenses.value ?: false)
        applyFilter()
    }

    // Apply filter based on current state
    private fun applyFilter() {
        val currentExpenses = _expenses.value ?: emptyList()

        if (_showOnlyMyExpenses.value == true) {
            // Get current user's username
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // This is a simplified approach - in a real app, you'd want to query the username once and store it
                viewModelScope.launch {
                    try {
                        val userDocument = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .get()
                            .await()

                        val username = userDocument.getString("Username") ?: ""
                        _filteredExpenses.value = currentExpenses.filter { it.paidBy == username }
                    } catch (e: Exception) {
                        _errorMessage.value = "Error filtering expenses: ${e.message}"
                        // If there's an error, still show all expenses
                        _filteredExpenses.value = currentExpenses
                    }
                }
            } else {
                // If user ID is null, show all expenses
                _filteredExpenses.value = currentExpenses
            }
        } else {
            // Show all expenses when filter is off
            _filteredExpenses.value = currentExpenses
        }
    }

    // Add a new expense
    fun addExpense(expense: Expense, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = expenseRepository.addExpense(expense)
                if (success) {
                    onSuccess()
                } else {
                    onError("Failed to add expense")
                }
            } catch (e: Exception) {
                onError("Error adding expense: ${e.message}")
            }
        }
    }

    // Update an expense
    fun updateExpense(expense: Expense, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = expenseRepository.updateExpense(expense)
                if (success) {
                    onSuccess()
                } else {
                    onError("Failed to update expense")
                }
            } catch (e: Exception) {
                onError("Error updating expense: ${e.message}")
            }
        }
    }

    // Delete an expense
    fun deleteExpense(expenseId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = expenseRepository.deleteExpense(expenseId)
                if (success) {
                    // Update the expenses list by removing the deleted expense
                    _expenses.value?.let { currentList ->
                        val updatedList = currentList.filter { it.id != expenseId }
                        _expenses.postValue(updatedList)

                        // Also update filtered expenses
                        _filteredExpenses.value?.let { currentFiltered ->
                            val updatedFiltered = currentFiltered.filter { it.id != expenseId }
                            _filteredExpenses.postValue(updatedFiltered)
                        }
                    }
                    onSuccess()
                } else {
                    onError("Failed to delete expense")
                }
            } catch (e: Exception) {
                onError("Error deleting expense: ${e.message}")
            }
        }
    }

    // Add this method to explicitly set the initial filter state
    fun setInitialFilterState() {
        _showOnlyMyExpenses.value = false
        applyFilter()
    }

    // Add method to update group in ViewModel
    fun updateGroupInViewModel(updatedGroup: Group) {
        _group.value = updatedGroup
    }

    // Modify the addExpenseToViewModel method to remove the scrollToTop flag
    fun addExpenseToViewModel(expense: Expense) {
        // Create a new list with the new expense at the beginning
        val currentList = _expenses.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, expense) // Add to the beginning of the list for chronological order
        _expenses.value = currentList

        // Also update the filtered expenses list
        val currentFiltered = _filteredExpenses.value?.toMutableList() ?: mutableListOf()

        // Only add to filtered list if it matches the current filter
        if (_showOnlyMyExpenses.value != true ||
            (_showOnlyMyExpenses.value == true && expense.paidBy == getCurrentUsername())) {
            currentFiltered.add(0, expense)
            _filteredExpenses.value = currentFiltered
        }
    }

    // Helper method to get current username
    private fun getCurrentUsername(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return ""
        var username = ""

        // This is a synchronous operation for simplicity
        // In a real app, you might want to cache the username
        try {
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .result

            username = document?.getString("Username") ?: ""
        } catch (e: Exception) {
            Log.e("GroupExpensesViewModel", "Error getting username", e)
        }

        return username
    }
}
