package com.example.androidproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.databinding.ExpenseItemBinding
import com.example.androidproject.model.Expense
import java.text.SimpleDateFormat
import java.util.Locale

class ExpensesAdapter(private val expenses: List<Expense>) :
    RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(private val binding: ExpenseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.expenseTitle.text = expense.Description
            binding.expenseDate.text = SimpleDateFormat(
                "dd/MM/yyyy HH:mm", Locale.getDefault()
            ).format(expense.Date!!.toDate())
            binding.expenseAmount.text = "${expense.Currency} ${expense.Amount}"

            if (expense.PaidBy == "You") {
                binding.expenseStatus.text = "${expense.PaidBy} owes you"
            } else {
                binding.expenseStatus.text = "You owe ${expense.PaidBy}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size
}