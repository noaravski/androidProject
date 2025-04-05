package com.example.androidproject.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidproject.Coroutines
import com.example.androidproject.model.Coin
import com.example.androidproject.repositories.CoinRepository
import kotlinx.coroutines.Job

class CoinListViewModel(private val repository: CoinRepository) : ViewModel() {
    private lateinit var job: Job
    private val _coins = MutableLiveData<List<Coin>>()
    val coins: LiveData<List<Coin>> get() = _coins
    fun fetchCoins(query: String) {
        job = Coroutines.ioTheMain(
            { repository.getCoins(query) },
            {
                println(it)
                _coins.value = it?.rates?.map { rate -> rate.coin }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (::job.isInitialized) job.cancel()
    }
}

