package com.example.androidproject.model

import com.google.gson.annotations.SerializedName

data class CoinResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("timestamp") val timestamp: Int,
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: List<Conversions>
)

data class Conversions(
    @SerializedName("coin") val coin: Coin
)