package com.example.androidproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.example.androidproject.model.User

class FriendsAdapter(private val friends: List<User>) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private var onItemClickListener: ((User) -> Unit)? = null

    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int = friends.size

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        private val friendName: TextView = itemView.findViewById(R.id.friendName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(friends[position])
                }
            }
        }

        fun bind(user: User) {
            // Ensure the view is updated even if data is null or empty
            friendName.text = user.Username.ifEmpty { "Unknown User" }

            // Load profile image with error handling
            try {
                if (!user.ImgUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(user.ImgUrl)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .circleCrop()
                        .into(profilePic)
                } else {
                    profilePic.setImageResource(R.drawable.profile)
                }
            } catch (e: Exception) {
                // Fallback to default image if there's any error
                profilePic.setImageResource(R.drawable.profile)
            }
        }
    }

    // Force refresh the adapter
    fun refresh() {
        notifyDataSetChanged()
    }
}
