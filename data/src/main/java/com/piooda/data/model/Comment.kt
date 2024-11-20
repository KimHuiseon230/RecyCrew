package com.piooda.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val author: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()  // 기본값을 현재 시간으로 설정
) : Parcelable
