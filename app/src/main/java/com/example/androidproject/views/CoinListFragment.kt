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
import com.example.androidproject.factories.RecipesViewModelFactory
import com.example.androidproject.model.Coin
import com.example.androidproject.repositories.CoinRepository
import com.example.androidproject.viewModels.CoinListViewModel

class CoinListFragment : Fragment(), CoinAdapter.OnCoinClickListener {
    private lateinit var factory: RecipesViewModelFactory
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: CoinAdapter
    private lateinit var viewModel: CoinListViewModel
    private lateinit var loader: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recycleview_coin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository = CoinRepository(CoinApi())
        factory = RecipesViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(CoinListViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerViewRecipe)
        loader = view.findViewById(R.id.progressBar)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recipeAdapter = CoinAdapter()
        recyclerView.adapter = recipeAdapter

        // Observe recipes LiveData from ViewModel
        viewModel.coins.observe(viewLifecycleOwner, Observer { coinResponse ->
            coinResponse?.let { // Check if recipeResponse is not null
                recipeAdapter.submitList(coinResponse)
                loader.visibility = View.GONE
            }

        })

        viewModel.fetchCoins("NIS")
        recipeAdapter.setOnRecipeClickListener(this)

    }
    override fun onCoinClick(coin: Coin) {
        println("click on: " + coin.currencyCode)
        val action = CoinListFragmentDirection.actionMusicalsListFragmentToMusicalFragment(coin)
        Navigation.findNavController(requireView()).navigate(action)
    }
}
