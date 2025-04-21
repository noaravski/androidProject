package com.example.androidproject.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidproject.model.Expense

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val description: String,
    val groupName: String,
    val paidBy: String,
    val date: Long,
    val imgUrl: String?,
    val currency: String
) {
    fun toExpense(): Expense {
        return Expense(
            id = id,
            amount = amount,
            description = description,
            groupName = groupName,
            paidBy = paidBy,
            date = date,
            imgUrl = imgUrl,
            currency = currency
        )
    }

    companion object {
        fun fromExpense(expense: Expense): ExpenseEntity {
            return ExpenseEntity(
                id = expense.id,
                amount = expense.amount,
                description = expense.description,
                groupName = expense.groupName,
                paidBy = expense.paidBy,
                date = expense.date,
                imgUrl = expense.imgUrl,
                currency = expense.currency
            )
        }
    }
}