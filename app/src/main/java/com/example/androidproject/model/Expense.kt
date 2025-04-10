package com.example.androidproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Expense(
    val id: String = "",
    var amount: Double = 0.0,
    val description: String = "",
    val groupName: String = "",
    val paidBy: String = "",
    val date: Long = 0,
    val imgUrl: String? = null,
    val currency: String = "USD"
) : Parcelable
