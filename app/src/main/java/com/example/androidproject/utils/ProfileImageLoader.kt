package com.example.androidproject.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Utility class to load user profile images consistently across the app
 */
class ProfileImageLoader {
    companion object {
        /**
         * Loads the current user's profile image into the provided ImageView
         */
        fun loadCurrentUserProfileImage(context: Context, imageView: ImageView) {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            val userId = auth.currentUser?.uid ?: return

            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileImageUrl = document.getString("ImgUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(context)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .circleCrop()
                                .into(imageView)
                        } else {
                            imageView.setImageResource(R.drawable.profile)
                        }
                    } else {
                        imageView.setImageResource(R.drawable.profile)
                    }
                }
                .addOnFailureListener {
                    // If loading fails, use default profile image
                    imageView.setImageResource(R.drawable.profile)
                }
        }

        /**
         * Loads a specific user's profile image into the provided ImageView
         */
        fun loadUserProfileImage(context: Context, userId: String, imageView: ImageView) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileImageUrl = document.getString("ImgUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(context)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .circleCrop()
                                .into(imageView)
                        } else {
                            imageView.setImageResource(R.drawable.profile)
                        }
                    } else {
                        imageView.setImageResource(R.drawable.profile)
                    }
                }
                .addOnFailureListener {
                    // If loading fails, use default profile image
                    imageView.setImageResource(R.drawable.profile)
                }
        }

        /**
         * Loads a group image into the provided ImageView
         */
        fun loadGroupImage(context: Context, groupId: String, imageView: ImageView) {
            val db = FirebaseFirestore.getInstance()

            db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val imageUrl = document.getString("imageUrl")
                        if (!imageUrl.isNullOrEmpty() && imageUrl != "default") {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.island)
                                .error(R.drawable.island)
                                .into(imageView)
                        } else {
                            imageView.setImageResource(R.drawable.island)
                        }
                    } else {
                        imageView.setImageResource(R.drawable.island)
                    }
                }
                .addOnFailureListener {
                    // If loading fails, use default image
                    imageView.setImageResource(R.drawable.island)
                }
        }

        /**
         * Loads an expense image into the provided ImageView
         */
        fun loadExpenseImage(context: Context, expenseId: String, imageView: ImageView) {
            val db = FirebaseFirestore.getInstance()

            db.collection("expenses").document(expenseId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val imageUrl = document.getString("imgUrl")
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_recipt)
                                .error(R.drawable.ic_recipt)
                                .into(imageView)
                        } else {
                            imageView.setImageResource(R.drawable.ic_recipt)
                        }
                    } else {
                        imageView.setImageResource(R.drawable.ic_recipt)
                    }
                }
                .addOnFailureListener {
                    // If loading fails, use default image
                    imageView.setImageResource(R.drawable.ic_recipt)
                }
        }
    }
}
