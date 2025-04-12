package com.example.androidproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val Uid: String = "",
    val Username: String = "",
    val Mail: String = "",
    val ImgUrl: String? = null,
    val Password: String? = null
) : Parcelable
