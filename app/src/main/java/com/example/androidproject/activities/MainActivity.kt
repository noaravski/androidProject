package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.adapters.FriendAdapter
import com.example.androidproject.adapters.GroupsAdapter
import com.example.androidproject.views.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.replace

class MainActivity : AppCompatActivity() {

    private lateinit var friendsTextView: TextView
    private lateinit var groupsTextView: TextView

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()

        friendsTextView = findViewById(R.id.friendsTextView)
        groupsTextView = findViewById(R.id.groupsTextView)

        selectTab(friendsTextView, R.layout.friend)
        fetchFriendsData()

        friendsTextView.setOnClickListener {
            selectTab(friendsTextView, R.layout.friend)
            fetchFriendsData()
        }

        groupsTextView.setOnClickListener {
            selectTab(groupsTextView, R.layout.vacation)
            fetchGroupsData()
        }

        val btnAddGroup: ImageView = findViewById(R.id.addGroupBtn)
        btnAddGroup.setOnClickListener {
            val intent = Intent(this, CreateGroupActivity::class.java)
            startActivity(intent)
        }

        val btnProfile: ImageView = findViewById(R.id.ivProfilePic)
        btnProfile.setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(android.R.id.content, ProfileFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

    }

    private fun selectTab(selectedTextView: TextView, layoutResId: Int) {
        friendsTextView.setTypeface(null, android.graphics.Typeface.NORMAL)
        groupsTextView.setTypeface(null, android.graphics.Typeface.NORMAL)
        selectedTextView.setTypeface(null, android.graphics.Typeface.BOLD)
    }


    private fun fetchFriendsData() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val friendsList = mutableListOf<String>()

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = FriendAdapter(friendsList)
        recyclerView.adapter = adapter

        firestore.collection("users").get().addOnSuccessListener { result ->
            for (document in result) {
                val userData = document.data
                friendsList.add(userData["Username"].toString())
            }
            val adapter = FriendAdapter(friendsList)
            recyclerView.adapter = adapter
        }.addOnFailureListener { exception ->
            println("Error getting documents: $exception")
        }
    }

    private fun fetchGroupsData() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val groupsList = mutableListOf<String>()

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = GroupsAdapter(groupsList)
        recyclerView.adapter = adapter

        firestore.collection("groups").get().addOnSuccessListener { result ->
            for (document in result) {
                val groupData = document.data
                groupsList.add(groupData["groupName"].toString())
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            println("Error getting documents: $exception")
        }
    }
}