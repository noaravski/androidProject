package com.example.androidproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.databinding.CoinRowBinding
import com.example.androidproject.model.Coin
import com.example.androidproject.views.CoinListFragment
import kotlin.math.roundToInt
import java.util.Currency

class CoinAdapter() : RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {
    private var coins: List<Coin> = ArrayList()
    var binding: CoinRowBinding? = null
    private var listener: OnCoinClickListener? = null


    inner class CoinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coinCodeTextView: TextView = itemView.findViewById(R.id.currency_code)
        private val coinMarkTextView: TextView = itemView.findViewById(R.id.currency_mark)

        fun bind(coin: Coin) {
            coinCodeTextView.text = coin.currencyCode.toString()
            coinMarkTextView.text = String.format(
                "%.2f %s",
                coin.conversionValue,
                Currency.getInstance(coin.currencyCode).symbol
            )
            itemView.setOnClickListener {
                listener?.onCoinClick(coin)
            }
        }
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

    override fun getItemCount(): Int {
        return coins.size
    }

    fun submitList(newList: List<Coin>) {
        val diffCallback = CoinDiffCallback(coins, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        coins = newList
        diffResult.dispatchUpdatesTo(this)
    }


    class CoinDiffCallback(
        private val oldList: List<Coin>, private val newList: List<Coin>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(
            oldItemPosition: Int, newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(
            oldItemPosition: Int, newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    interface OnCoinClickListener {
        fun onCoinClick(coin: Coin)
    }

    fun setOnCoinClickListener(listener: CoinListFragment) {
        this.listener = listener
    }
}