package com.example.androidproject.api

import com.example.androidproject.model.CoinResponse;
import com.google.gson.GsonBuilder;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface CoinApi {
    @GET("/latest")
    suspend fun getConversionValue(
        @Query("access_key") apiKey: String,
        @Query("base") baseCoin: String,
        @Query("symbols") symbols: String,
    ): Response<CoinResponse>

    companion object {
        operator fun invoke(): CoinApi {
            val url = "https://api.exchangeratesapi.io/v1"

            val gson = GsonBuilder().setLenient().create()

            return Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
                .create(CoinApi::class.java)
        }
    }
}