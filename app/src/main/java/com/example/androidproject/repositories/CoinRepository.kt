package com.example.androidproject.repositories

import com.example.androidproject.api.CoinApi
import com.example.androidproject.api.SafeApiRequest

class CoinRepository(private val api: CoinApi) : SafeApiRequest() {
    suspend fun getCoins() = apiRequest {
        api.getConversionValue(
            access_key = "0b4d6e0e5dc0ed484e584f986964af68",
            symbols = "USD,ILS,THB,EUR"
        )
    }
}