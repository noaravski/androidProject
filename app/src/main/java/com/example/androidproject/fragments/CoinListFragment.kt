package com.example.androidproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.adapters.CoinAdapter
import com.example.androidproject.model.Coin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class CoinListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var coinAdapter: CoinAdapter
    private val coinList = mutableListOf<Coin>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recycleview_coin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewCoin)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapter
        coinAdapter = CoinAdapter(coinList)
        recyclerView.adapter = coinAdapter

        // Fetch coin data
        fetchCoinData()
    }

    private fun fetchCoinData() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val coinApiService = retrofit.create(CoinApiService::class.java)

        coinApiService.getCoins().enqueue(object : Callback<List<CoinResponse>> {
            override fun onResponse(call: Call<List<CoinResponse>>, response: Response<List<CoinResponse>>) {
                if (response.isSuccessful) {
                    val coins = response.body()
                    coins?.let {
                        val mappedCoins = it.map { coinResponse ->
                            Coin(
                                id = coinResponse.id,
                                symbol = coinResponse.symbol.uppercase(),
                                name = coinResponse.name,
                                currencySymbol = getCurrencySymbol(coinResponse.symbol)
                            )
                        }
                        coinList.clear()
                        coinList.addAll(mappedCoins)
                        coinAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<List<CoinResponse>>, t: Throwable) {
                // Handle error
                t.printStackTrace()
            }
        })
    }

    private fun getCurrencySymbol(symbol: String): String {
        return when (symbol.lowercase()) {
            "usd" -> "$"
            "eur" -> "€"
            "gbp" -> "£"
            "jpy" -> "¥"
            "btc" -> "₿"
            "eth" -> "Ξ"
            else -> symbol.uppercase()
        }
    }

    // API Service interface
    interface CoinApiService {
        @GET("coins/markets?vs_currency=usd&order=market_cap_desc&per_page=20&page=1")
        fun getCoins(): Call<List<CoinResponse>>
    }

    // Response data class
    data class CoinResponse(
        val id: String,
        val symbol: String,
        val name: String,
        val current_price: Double
    )
}
