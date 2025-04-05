package com.example.androidproject.repositories

import com.example.androidproject.api.CoinApi
import com.example.androidproject.api.SafeApiRequest

class CoinRepository(private val api: CoinApi) : SafeApiRequest() {
    suspend fun getCoins(query: String) = apiRequest {
        api.getConversionValue(
            apiKey = "922d92f21ae36578e9a9dd5b128bdbd2",
            baseCoin = query,
            symbols = "USD,EUR,NIS,THB"
        )
    }
}