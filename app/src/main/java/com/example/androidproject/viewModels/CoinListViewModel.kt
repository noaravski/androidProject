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
    fun fetchCoins() {
        job = Coroutines.ioTheMain(
            { repository.getCoins() },
            {
                println(it)
                _coins.value = it?.rates?.map { rate -> Coin(rate.key, rate.value) }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (::job.isInitialized) job.cancel()
    }
}

