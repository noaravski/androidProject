package com.example.androidproject.activities

import android.os.Bundle
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
        setContentView(R.layout.activity_login)
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.main_navhost) as NavHostFragment?
//        navController = navHostFragment!!.navController
//        val navView = findViewById<BottomNavigationView>(R.id.main_bottomNavigation)
//        setupWithNavController(navView, navController!!)
    }
}