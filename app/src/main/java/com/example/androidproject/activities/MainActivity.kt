package com.example.androidproject.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.androidproject.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onFriendsTabClicked(view: View) {
        findViewById<TextView>(R.id.tvFriendsTab).apply {
            setBackgroundResource(R.drawable.tab_selected_background)
            setTextColor(Color.BLACK)
        }
        findViewById<TextView>(R.id.tvGroupsTab).apply {
            setBackgroundResource(0)
            setTextColor(Color.parseColor("#757575"))
        }
    }

    fun onGroupsTabClicked(view: View) {
        findViewById<TextView>(R.id.tvGroupsTab).apply {
            setBackgroundResource(R.drawable.tab_selected_background)
            setTextColor(Color.BLACK)
        }
        findViewById<TextView>(R.id.tvFriendsTab).apply {
            setBackgroundResource(0)
            setTextColor(Color.parseColor("#757575"))
        }
    }
}