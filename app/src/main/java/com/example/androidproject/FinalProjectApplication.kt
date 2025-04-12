package com.example.androidproject

import android.app.Application
import com.example.androidproject.utils.CloudinaryHelper

class FinalProjectApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Cloudinary
        CloudinaryHelper.init(this)
    }
}
