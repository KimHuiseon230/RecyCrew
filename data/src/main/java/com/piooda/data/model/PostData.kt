package com.piooda.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostData(
    val postId: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val imagePath: String = "",
    val userName: String = "",
    val category: String = "",
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val time: String = "",
    ) : Parcelable





