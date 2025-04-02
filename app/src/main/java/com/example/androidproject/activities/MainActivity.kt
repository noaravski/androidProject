package com.example.androidproject.activities

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.androidproject.R
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


        friendsTextView.setOnClickListener {
            selectTab(friendsTextView, R.layout.friend)
            fetchFriendsData()
        }

        groupsTextView.setOnClickListener {
            selectTab(groupsTextView, R.layout.vacation)
        }
    }


    private fun selectTab(selectedTextView: TextView, layoutResId: Int) {
        friendsTextView.setBackgroundResource(0)
        groupsTextView.setBackgroundResource(0)
        selectedTextView.setBackgroundResource(R.drawable.tab_selected_background)

        val fragment = LayoutFragment.newInstance(layoutResId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun fetchFriendsData() {
        val friendsContainer = LayoutInflater.from(this).inflate(R.layout.friend, null) as LinearLayout
        friendsContainer.removeAllViews()

        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userData = document.data
                    val friendView = LayoutInflater.from(this).inflate(R.layout.friend, friendsContainer, false)

                    // Populate the friendView with userData
                    val nameTextView = friendView.findViewById<TextView>(R.id.friendName)
                    val balanceTextView = friendView.findViewById<TextView>(R.id.balance)
//                    val balanceTotalTextView = friendView.findViewById<TextView>(R.id.balanceTotal)

                    nameTextView.text = userData["name"].toString()
                    balanceTextView.text = userData["balance"].toString()
                    if(userData["balance"].toString().toInt() > 0){
                        balanceTextView.setTextColor(Color.GREEN)
                    } else {
                        balanceTextView.setTextColor(Color.RED)
                    }
//                    balanceTotalTextView.text = userData["balanceTotal"].toString()

                    // Add the friendView to the container
                    friendsContainer.addView(friendView)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

}