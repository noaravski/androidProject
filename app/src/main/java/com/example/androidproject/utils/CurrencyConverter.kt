package com.example.androidproject.utils

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object CurrencyConverter {
    private const val TAG = "CurrencyConverter"
    private const val BASE_URL = "https://api.exchangerate-api.com/v4/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ExchangeRateApiService::class.java)

    // Hardcoded exchange rates as fallback in case API fails
    private val fallbackRates = mapOf(
        "USD" to mapOf(
            "EUR" to 0.92,
            "GBP" to 0.79,
            "JPY" to 150.0,
            "CAD" to 1.36,
            "AUD" to 1.52,
            "ILS" to 3.67
        ),
        "EUR" to mapOf(
            "USD" to 1.09,
            "GBP" to 0.86,
            "JPY" to 163.0,
            "CAD" to 1.48,
            "AUD" to 1.65,
            "ILS" to 3.99
        ),
        "GBP" to mapOf(
            "USD" to 1.27,
            "EUR" to 1.16,
            "JPY" to 190.0,
            "CAD" to 1.72,
            "AUD" to 1.92,
            "ILS" to 4.65
        ),
        "JPY" to mapOf(
            "USD" to 0.0067,
            "EUR" to 0.0061,
            "GBP" to 0.0053,
            "CAD" to 0.0091,
            "AUD" to 0.0101,
            "ILS" to 0.0245
        ),
        "CAD" to mapOf(
            "USD" to 0.74,
            "EUR" to 0.68,
            "GBP" to 0.58,
            "JPY" to 110.0,
            "AUD" to 1.12,
            "ILS" to 2.70
        ),
        "AUD" to mapOf(
            "USD" to 0.66,
            "EUR" to 0.61,
            "GBP" to 0.52,
            "JPY" to 99.0,
            "CAD" to 0.89,
            "ILS" to 2.42
        ),
        "ILS" to mapOf(
            "USD" to 0.27,
            "EUR" to 0.25,
            "GBP" to 0.22,
            "JPY" to 40.9,
            "CAD" to 0.37,
            "AUD" to 0.41
        )
    )

    fun getExchangeRate(
        fromCurrency: String,
        toCurrency: String,
        onSuccess: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        // If currencies are the same, rate is 1.0
        if (fromCurrency == toCurrency) {
            onSuccess(1.0)
            return
        }

        // Try to get exchange rate from API
        apiService.getLatestRates(fromCurrency).enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful) {
                    val rateResponse = response.body()
                    if (rateResponse != null && rateResponse.rates.containsKey(toCurrency)) {
                        val rate = rateResponse.rates[toCurrency] ?: 1.0
                        Log.d(TAG, "API Exchange rate from $fromCurrency to $toCurrency: $rate")
                        onSuccess(rate)
                    } else {
                        // Use fallback rates if API doesn't have the requested currency
                        useFallbackRate(fromCurrency, toCurrency, onSuccess, onError)
                    }
                } else {
                    // Use fallback rates if API call fails
                    useFallbackRate(fromCurrency, toCurrency, onSuccess, onError)
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}")
                // Use fallback rates if API call fails
                useFallbackRate(fromCurrency, toCurrency, onSuccess, onError)
            }
        })
    }

    private fun useFallbackRate(
        fromCurrency: String,
        toCurrency: String,
        onSuccess: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val fromRates = fallbackRates[fromCurrency]
        if (fromRates != null && fromRates.containsKey(toCurrency)) {
            val rate = fromRates[toCurrency] ?: 1.0
            Log.d(TAG, "Fallback exchange rate from $fromCurrency to $toCurrency: $rate")
            onSuccess(rate)
        } else if (fallbackRates[toCurrency]?.containsKey(fromCurrency) == true) {
            // Try reverse lookup and invert the rate
            val reverseRate = fallbackRates[toCurrency]?.get(fromCurrency) ?: 1.0
            val rate = 1.0 / reverseRate
            Log.d(TAG, "Fallback (inverted) exchange rate from $fromCurrency to $toCurrency: $rate")
            onSuccess(rate)
        } else {
            // If no fallback rate is available, use 1.0 as default
            Log.w(TAG, "No fallback rate available for $fromCurrency to $toCurrency, using 1.0")
            onSuccess(1.0)
        }
    }

    interface ExchangeRateApiService {
        @GET("latest/USD")
        fun getLatestRates(@Query("base") baseCurrency: String): Call<ExchangeRateResponse>
    }

    data class ExchangeRateResponse(
        val base: String,
        val date: String,
        val rates: Map<String, Double>
    )
}
