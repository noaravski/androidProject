package com.example.androidproject.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.R
import com.example.androidproject.views.CoinListFragment

class RecycleViewCoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycleview_coin)

        val fragment = CoinListFragment().apply {
            arguments = Bundle().apply {
                putString("GROUP_NAME", intent.getStringExtra("GROUP_NAME"))
                putString("CURRENCY_NAME", intent.getStringExtra("CURRENCY_NAME"))
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}