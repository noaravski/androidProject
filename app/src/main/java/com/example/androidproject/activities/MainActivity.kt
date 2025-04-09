package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.example.androidproject.adapters.FriendAdapter
import com.example.androidproject.adapters.GroupsAdapter
import com.example.androidproject.model.Friend
import com.example.androidproject.model.Vacation
import com.example.androidproject.views.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.get

class MainActivity : AppCompatActivity() {

    private lateinit var profilePic: ImageView
    private lateinit var friendsTextView: TextView
    private lateinit var groupsTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var addGroupBtn: ImageView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI elements
        profilePic = findViewById(R.id.ivProfilePic)
        addGroupBtn = findViewById(R.id.addGroupBtn)
        friendsTextView = findViewById(R.id.friendsTextView)
        groupsTextView = findViewById(R.id.groupsTextView)
        recyclerView = findViewById(R.id.recyclerView)

        // Set up click listeners
        profilePic.setOnClickListener { openProfileFragment() }
        addGroupBtn.setOnClickListener { openCreateGroupActivity() }

        // Set up tab click listeners
        friendsTextView.setOnClickListener { switchToFriendsTab() }
        groupsTextView.setOnClickListener { switchToGroupsTab() }


        //load profile pic
        loadUserProfileImage()

        // Default to friends tab
        switchToFriendsTab()
    }

    private fun loadUserProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                val imageUrl = document.getString("ImgUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imageUrl)
                        .placeholder(R.drawable.profile) // Default placeholder
                        .circleCrop()
                        .error(R.drawable.profile) // Error placeholder
                        .into(profilePic)
                }
            }.addOnFailureListener { exception ->
                println("Error fetching profile image: $exception")
            }
    }

    private fun openProfileFragment() {
        // Navigate to profile fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, ProfileFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openCreateGroupActivity() {
        // Navigate to create group activity
        val intent = Intent(this@MainActivity, CreateGroupActivity::class.java)
        startActivity(intent)
    }

    private fun switchToFriendsTab() {
        friendsTextView.setBackgroundResource(R.drawable.tab_selected_background)
        friendsTextView.setTextColor(resources.getColor(R.color.black))
        groupsTextView.setBackgroundResource(android.R.color.transparent)
        groupsTextView.setTextColor(resources.getColor(android.R.color.darker_gray))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val friendsList = mutableListOf<Friend>()

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = FriendAdapter(friendsList)
        recyclerView.adapter = adapter

        firestore.collection("users").get().addOnSuccessListener { result ->
            for (document in result) {
                val username = document.getString("Username").orEmpty()
                val imageUrl = document.getString("ImgUrl").orEmpty()
                friendsList.add(Friend(username, imageUrl))
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            println("Error getting documents: $exception")
        }
    }

    private fun switchToGroupsTab() {
        groupsTextView.setBackgroundResource(R.drawable.tab_selected_background)
        groupsTextView.setTextColor(resources.getColor(R.color.black))
        friendsTextView.setBackgroundResource(android.R.color.transparent)
        friendsTextView.setTextColor(resources.getColor(android.R.color.darker_gray))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val groupsList = mutableListOf<Vacation>()

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = GroupsAdapter(groupsList)
        recyclerView.adapter = adapter

        firestore.collection("groups").get().addOnSuccessListener { result ->
            for (document in result) {
                val groupData = document.data
                groupsList.add(
                    Vacation(
                        groupData["groupName"].toString(), groupData["imageUrl"].toString()
                    )
                )
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            println("Error getting documents: $exception")
        }
    }
}
