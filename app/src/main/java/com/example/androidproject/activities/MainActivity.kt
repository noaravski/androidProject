package com.example.androidproject.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.adapters.FriendAdapter
import com.example.androidproject.adapters.GroupsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private var navController: NavController? = null

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

        val changeCurrencyButton: TextView = findViewById(R.id.changeCurrency)
        changeCurrencyButton.setOnClickListener {
            inflateRecycleViewCoin()
        }
    }

    private fun inflateRecycleViewCoin() {
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_graph) as NavHostFragment).navController
        navController?.navigate(R.id.action_mainActivity_to_coinListFragment)
    }

    private fun selectTab(selectedTextView: TextView, layoutResId: Int) {
        friendsTextView.setBackgroundResource(0)
        groupsTextView.setBackgroundResource(0)
        selectedTextView.setBackgroundResource(R.drawable.tab_selected_background)
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
            val adapter = GroupsAdapter(groupsList)
            recyclerView.adapter = adapter
        }.addOnFailureListener { exception ->
            println("Error getting documents: $exception")
        }
    }
}