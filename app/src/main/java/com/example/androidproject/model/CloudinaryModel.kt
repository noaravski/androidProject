package com.example.androidproject.model

import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import com.cloudinary.android.callback.ErrorInfo
import com.example.androidproject.extensions.toFile
import com.example.androidproject.BuildConfig
import com.example.androidproject.FinalProjectApplication.getMyContext

class CloudinaryModel {

    private var mediaManager: MediaManager? = null

    private fun ensureMediaManagerInitialized() {
        if (mediaManager == null) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUD_NAME,
                "api_key" to BuildConfig.API_KEY,
                "api_secret" to BuildConfig.API_SECRET
            )
            MediaManager.init(getMyContext(), config)
            mediaManager = MediaManager.get()
            mediaManager?.globalUploadPolicy = GlobalUploadPolicy.defaultPolicy()
        }
    }

    private fun getMediaManager(): MediaManager {
        ensureMediaManagerInitialized()
        return mediaManager!!
    }

    fun uploadImage(
        bitmap: Bitmap, name: String, onSuccess: (String?) -> Unit, onError: (String?) -> Unit
    ) {
        val context = getMyContext()
        val file: File = bitmap.toFile(context, name)

        Log.d("CloudinaryModel", "Uploading file: ${file.path}")

        getMediaManager().upload(file.path).option("folder", "uploads")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("CloudinaryModel", "Upload started: $requestId")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    Log.d("CloudinaryModel", "Upload progress: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    Log.d("CloudinaryModel", "Upload success: $url")
                    onSuccess(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("CloudinaryModel", "Upload error: ${error?.description}")
                    onError(error?.description ?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.d("CloudinaryModel", "Upload rescheduled: ${error?.description}")
                }
            }).dispatch()
    }
}