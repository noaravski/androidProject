package com.example.androidproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Group(
    val id: String = "",
    val groupName: String = "",
    val description: String = "",
    val createdBy: String = "", // Changed to always be a String
    val createdAt: Long = 0,
    val imageUrl: String? = null,
    var currency: String = "USD",
    val members: List<String> = listOf()
) : Parcelable
