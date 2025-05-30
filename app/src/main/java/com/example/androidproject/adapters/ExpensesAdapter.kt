package com.example.androidproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.example.androidproject.model.Expense
import com.example.androidproject.utils.ProfileImageLoader.Companion.convertToHttps
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.DiffUtil

class ExpensesAdapter(private var expenses: List<Expense>) :
    RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    private var onItemClickListener: ((Expense) -> Unit)? = null

    fun setOnItemClickListener(listener: (Expense) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expense_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int = expenses.size

    // Add a method to update the expenses list
    fun updateExpenses(newExpenses: List<Expense>) {
        val oldList = expenses
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newExpenses.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newExpenses[newItemPosition].id
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newExpenses[newItemPosition]
            }
        })

        // Update the expenses list
        expenses = newExpenses
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val expenseImage: ImageView = itemView.findViewById(R.id.expenseImage)
        private val expenseTitle: TextView = itemView.findViewById(R.id.expenseTitle)
        private val expenseDate: TextView = itemView.findViewById(R.id.expenseDate)
        private val expenseStatus: TextView = itemView.findViewById(R.id.expenseStatus)
        private val expenseAmount: TextView = itemView.findViewById(R.id.expenseAmount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(expenses[position])
                }
            }
        }

        fun bind(expense: Expense) {
            expenseTitle.text = expense.description

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = Date(expense.date)
            expenseDate.text = dateFormat.format(date)

            // Set status (placeholder)
            expenseStatus.text = "Paid by: ${expense.paidBy}"

            // Format amount with currency
            val currencySymbol = getCurrencySymbol(expense.currency)
            expenseAmount.text = String.format("%s%.2f", currencySymbol, expense.amount)

            // Load expense image
            if (!expense.imgUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(convertToHttps(expense.imgUrl))
                    .placeholder(R.drawable.ic_recipt)
                    .error(R.drawable.ic_recipt)
                    .into(expenseImage)
            } else {
                expenseImage.setImageResource(R.drawable.ic_recipt)
            }
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
    }
}
