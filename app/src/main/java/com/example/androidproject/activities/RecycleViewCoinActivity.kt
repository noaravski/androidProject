package com.example.androidproject.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.R
import com.example.androidproject.views.CoinListFragment

class RecycleViewCoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycleview_coin)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CoinListFragment())
            .commit()
    }
}