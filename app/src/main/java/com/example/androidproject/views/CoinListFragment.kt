package com.example.androidproject.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.CoinAdapter
import com.example.androidproject.R
import com.example.androidproject.api.CoinApi
import com.example.androidproject.factories.CoinsViewModelFactory
import com.example.androidproject.model.Coin
import com.example.androidproject.repositories.CoinRepository
import com.example.androidproject.viewModels.CoinListViewModel

class CoinListFragment : Fragment(), CoinAdapter.OnCoinClickListener {
    private lateinit var factory: CoinsViewModelFactory
    private lateinit var recyclerView: RecyclerView
    private lateinit var coinAdapter: CoinAdapter
    private lateinit var viewModel: CoinListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recycleview_coin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository = CoinRepository(CoinApi())
        factory = CoinsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CoinListViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerViewCoin)
        coinAdapter = CoinAdapter()
        recyclerView.adapter = coinAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        viewModel.coins.observe(viewLifecycleOwner, Observer { coinResponse ->
            coinResponse?.let {
                coinAdapter.submitList(coinResponse)
            }

        })

        viewModel.fetchCoins()
        coinAdapter.setOnCoinClickListener(this)

    }
    override fun onCoinClick(coin: Coin) {
        println("click on: " + coin.currencyCode)
//        val action = CoinListFragmentDirection.actionMusicalsListFragmentToMusicalFragment(coin)
//        Navigation.findNavController(requireView()).navigate(action)
    }
}
