package com.piooda.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val author: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable