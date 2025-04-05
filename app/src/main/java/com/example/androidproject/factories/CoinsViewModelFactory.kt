package com.example.androidproject.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidproject.repositories.CoinRepository
import com.example.androidproject.viewModels.CoinListViewModel

class CoinsViewModelFactory(private val repository: CoinRepository):
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CoinListViewModel(repository) as T
    }
}