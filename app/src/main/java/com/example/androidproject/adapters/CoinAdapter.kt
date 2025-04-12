package com.example.androidproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.model.Coin

class CoinAdapter(private val coins: List<Coin>) :
    RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {

    private var onItemClickListener: ((Coin) -> Unit)? = null

    fun setOnItemClickListener(listener: (Coin) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.coin_row, parent, false)
        return CoinViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coins[position]
        holder.bind(coin)
    }

    override fun getItemCount(): Int = coins.size

    inner class CoinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val currencyCode: TextView = itemView.findViewById(R.id.currency_code)
        private val currencyMark: TextView = itemView.findViewById(R.id.currency_mark)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(coins[position])
                }
            }
        }

        fun bind(coin: Coin) {
            currencyCode.text = coin.symbol
            currencyMark.text = coin.currencySymbol
        }
    }
}
