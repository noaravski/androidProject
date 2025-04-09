package com.example.androidproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.example.androidproject.model.Friend

class FriendAdapter(private val friendsList: List<Friend>) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendName: TextView = itemView.findViewById(R.id.friendName)
        val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]
        holder.friendName.text = friend.username

        Glide.with(holder.itemView.context)
            .load(friend.imageUrl)
            .circleCrop()
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(holder.profilePic)
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }
}