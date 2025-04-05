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
        @Query("access_key") access_key: String,
        @Query("symbols") symbols: String,
//        @Query("base") baseCoin: String,
    ): Response<CoinResponse>

    companion object {
        operator fun invoke(): CoinApi {
            val url = "https://data.fixer.io/api/latest/"

            val gson = GsonBuilder().setLenient().create()

            return Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
                .create(CoinApi::class.java)
        }
    }
}