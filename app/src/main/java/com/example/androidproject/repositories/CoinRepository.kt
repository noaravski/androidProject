package com.example.androidproject.repositories

import com.example.androidproject.api.CoinApi
import com.example.androidproject.api.SafeApiRequest

class CoinRepository(private val api: CoinApi) : SafeApiRequest() {
    suspend fun getCoins() = apiRequest {
        api.getConversionValue(
            access_key = "356f0c7fb8a7ff272314bd149bff3e99",
            symbols = "USD,ILS,THB,EUR"
        )
    }
}