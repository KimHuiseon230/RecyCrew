package com.piooda.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostData(
    val title: String = "",
    val content: String = "",
    val imagePath: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val category: String = "",
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val time: String = "",
    val comments: List<Comment> = emptyList()
) : Parcelable

