package com.example.androidproject.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.example.androidproject.activities.GroupExpensesActivity
import com.example.androidproject.model.Vacation

class GroupsAdapter(private val groupsList: List<Vacation>) :
    RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vacation, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.groupNameTextView.text = groupsList[position].groupName

        Glide.with(holder.itemView.context)
            .load(groupsList[position].imageUrl)
            .circleCrop()
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(holder.profileImageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, GroupExpensesActivity::class.java)
            intent.putExtra("GROUP_NAME", groupsList[position].groupName)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupNameTextView: TextView = itemView.findViewById(R.id.groupName)
        val profileImageView: ImageView = itemView.findViewById(R.id.ivProfilePic2)
    }

}