package com.example.androidproject.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.util.HashMap

class CloudinaryHelper {
    companion object {
        private const val TAG = "CloudinaryHelper"
        private var isInitialized = false

        fun init(context: Context) {
            if (!isInitialized) {
                try {
                    val config = HashMap<String, String>()
                    config["cloud_name"] = "dz4ryazjv"
                    config["api_key"] = "772156473141549"
                    config["api_secret"] = "vdzRMXa0KGQcbC-pLSSB_iMUjZA"
                    config["secure"] = "true"

                    MediaManager.init(context, config)
                    isInitialized = true
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing Cloudinary", e)
                }
            }
        }

        fun uploadImage(
            context: Context,
            imageUri: Uri,
            folder: String,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
        ) {
            if (!isInitialized) {
                init(context)
            }

            val requestId = MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = bytes * 100 / totalBytes
                        Log.d(TAG, "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        Log.d(TAG, "Upload success: $requestId")
                        val url = resultData["url"] as String
                        onSuccess(url)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Upload error: ${error.description}")
                        onError(error.description)
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d(TAG, "Upload rescheduled: $requestId")
                    }
                })
                .dispatch()
        }
    }
}
