package com.example.androidproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.databinding.ExpenseItemBinding
import com.example.androidproject.model.Expense
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

class ExpensesAdapter(private val expenses: List<Expense>) :
    RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(private val binding: ExpenseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.expenseTitle.text = expense.description
            binding.expenseDate.text = SimpleDateFormat(
                "dd/MM/yyyy HH:mm", Locale.getDefault()
            ).format(expense.date!!.toDate())
            binding.expenseAmount.text = expense.amount.toString()

            if (expense.paidBy == "Self") {
                binding.expenseStatus.text = "${expense.paidBy} owes you"
            } else {
                binding.expenseStatus.text = "You owe ${expense.paidBy}"
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