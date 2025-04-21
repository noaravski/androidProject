package com.example.androidproject.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.androidproject.data.room.AppDatabase
import com.example.androidproject.data.room.ExpenseEntity
import com.example.androidproject.model.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ExpenseRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val expenseDao = AppDatabase.getDatabase(context).expenseDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Get expenses for a group with LiveData
    fun getExpensesByGroup(groupId: String): LiveData<List<Expense>> {
        // Fetch from Firestore and update Room
        refreshExpenses(groupId)

        // Return LiveData from Room
        return expenseDao.getExpensesByGroup(groupId).map { entities ->
            entities.map { it.toExpense() }
        }
    }

    // Get a single expense by ID
    fun getExpenseById(expenseId: String): LiveData<Expense> {
        val result = MutableLiveData<Expense>()

        coroutineScope.launch {
            try {
                // Try to get from Room first
                val localData = expenseDao.getExpenseById(expenseId)

                // If not in Room, fetch from Firestore
                if (localData.value == null) {
                    val document = db.collection("expenses").document(expenseId).get().await()
                    if (document.exists()) {
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

                        // Save to Room
                        expenseDao.insertExpense(ExpenseEntity.fromExpense(expense))

                        withContext(Dispatchers.Main) {
                            result.value = expense
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        result.value = localData.value?.toExpense()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }

        return result
    }

    // Ensure expenses are always sorted by date in descending order
    private fun refreshExpenses(groupId: String) {
        coroutineScope.launch {
            try {
                // Debug log
                android.util.Log.d("ExpenseRepository", "Refreshing expenses for group $groupId")

                val snapshot = db.collection("expenses")
                    .whereEqualTo("groupName", groupId)
                    .orderBy("date", Query.Direction.DESCENDING) // Ensure newest expenses are first
                    .get()
                    .await()

                android.util.Log.d("ExpenseRepository", "Fetched ${snapshot.documents.size} expenses from Firestore")

                val expenses = snapshot.documents.map { document ->
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

                // Save to Room
                val entities = expenses.map { ExpenseEntity.fromExpense(it) }
                expenseDao.insertExpenses(entities)

                android.util.Log.d("ExpenseRepository", "Saved ${entities.size} expenses to Room")
            } catch (e: Exception) {
                android.util.Log.e("ExpenseRepository", "Error refreshing expenses", e)
                // Handle error
            }
        }
    }

    // Add a new expense
    suspend fun addExpense(expense: Expense): Boolean {
        return try {
            // Add to Firestore
            val expenseMap = hashMapOf(
                "amount" to expense.amount,
                "description" to expense.description,
                "groupName" to expense.groupName,
                "paidBy" to expense.paidBy,
                "date" to expense.date,
                "currency" to expense.currency,
                "imgUrl" to (expense.imgUrl ?: "")
            )

            db.collection("expenses").document(expense.id).set(expenseMap).await()

            // Add to Room
            expenseDao.insertExpense(ExpenseEntity.fromExpense(expense))
            true
        } catch (e: Exception) {
            false
        }
    }

    // Update an expense
    suspend fun updateExpense(expense: Expense): Boolean {
        return try {
            // Update in Firestore
            val updates = hashMapOf<String, Any>(
                "description" to expense.description,
                "amount" to expense.amount,
                "date" to expense.date,
                "imgUrl" to (expense.imgUrl ?: "")
            )

            db.collection("expenses").document(expense.id).update(updates).await()

            // Update in Room
            expenseDao.insertExpense(ExpenseEntity.fromExpense(expense))
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete an expense
    suspend fun deleteExpense(expenseId: String): Boolean {
        return try {
            // Delete from Firestore
            db.collection("expenses").document(expenseId).delete().await()

            // Delete from Room
            expenseDao.deleteExpense(expenseId)

            // Log for debugging
            android.util.Log.d("ExpenseRepository", "Successfully deleted expense $expenseId")

            true
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepository", "Error deleting expense", e)
            false
        }
    }
}
