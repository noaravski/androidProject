package com.example.androidproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val currencySymbol: String
) : Parcelable
