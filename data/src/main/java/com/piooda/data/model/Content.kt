package com.piooda.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.util.Date


// ✅ Firestore 데이터 전송을 위한 DTO
data class ContentDto(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val createdDate: Date? = null,
    var favoriteCount: Int = 0,
    val imagePath: String = "",
    var commentCount: Int = 0,
    var viewCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap()
) {
    @Parcelize
    data class Comment(
        val author: String = "",
        val content: String = "",
        val timestamp: Timestamp = Timestamp.now()
    ) : Parcelable
}

// ✅ 앱 내 데이터 사용을 위한 Content (Serializable 적용)
data class Content(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val createdDate: Date = Date(),
    var favoriteCount: Int = 0,
    val imagePath: String = "",
    var commentCount: Int = 0,
    var viewCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap()
) : java.io.Serializable {

    @Parcelize
    data class Comment(
        val author: String = "",
        val content: String = "",
        val timestamp: Timestamp = Timestamp.now()
    ) : Parcelable
}
