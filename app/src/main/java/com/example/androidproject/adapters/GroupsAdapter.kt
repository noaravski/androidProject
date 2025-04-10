package com.example.androidproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.example.androidproject.model.Group
import android.widget.Toast

class GroupsAdapter(
    private val groups: List<Group>,
    private val currentUserId: String
) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    private var onItemClickListener: ((Group) -> Unit)? = null
    private var onJoinClickListener: ((Group) -> Unit)? = null

    fun setOnItemClickListener(listener: (Group) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnJoinClickListener(listener: (Group) -> Unit) {
        onJoinClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_item, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int = groups.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupImage: ImageView = itemView.findViewById(R.id.groupImage)
        private val groupName: TextView = itemView.findViewById(R.id.groupName)
        private val groupDescription: TextView = itemView.findViewById(R.id.groupDescription)
        private val joinButton: Button = itemView.findViewById(R.id.joinButton)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val group = groups[position]
                    // Only allow clicking on groups the user is a member of
                    if (group.members.contains(currentUserId)) {
                        onItemClickListener?.invoke(group)
                    } else {
                        // If not a member, show a toast suggesting to join first
                        Toast.makeText(itemView.context, "Join this group to view expenses", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            joinButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onJoinClickListener?.invoke(groups[position])
                }
            }
        }

        fun bind(group: Group) {
            groupName.text = group.groupName
            groupDescription.text = group.description

            // Show join/exit button based on membership
            if (group.members.contains(currentUserId)) {
                joinButton.text = "Exit"
                joinButton.setBackgroundColor(itemView.context.getColor(R.color.purple_700))
            } else {
                joinButton.text = "Join"
                joinButton.setBackgroundColor(itemView.context.getColor(R.color.teal_primary))
            }

            // Load group image
            if (!group.imageUrl.isNullOrEmpty() && group.imageUrl != "default") {
                Glide.with(itemView.context)
                    .load(group.imageUrl)
                    .placeholder(R.drawable.island)
                    .error(R.drawable.island)
                    .into(groupImage)
            } else {
                groupImage.setImageResource(R.drawable.island)
            }
        }
    }
}
