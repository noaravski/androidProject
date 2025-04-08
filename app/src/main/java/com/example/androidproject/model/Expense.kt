package com.example.androidproject.model

data class Expense(
    val groupName: String?,
    val description: String?,
    val amount: Double?,
    val date: com.google.firebase.Timestamp?,
    val imgUrl: String?,
    val paidBy: String?,
)