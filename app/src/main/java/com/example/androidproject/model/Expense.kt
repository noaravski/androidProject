package com.example.androidproject.model

data class Expense(
    val GroupName: String?,
    val Description: String?,
    val Amount: Double?,
    val Currency: String?,
    val Date: com.google.firebase.Timestamp?,
    val ImgUrl: String?,
    val PaidBy: String?,
    val SplitBetween: List<String>?
)