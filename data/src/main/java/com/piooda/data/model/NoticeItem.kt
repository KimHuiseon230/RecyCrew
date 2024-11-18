package com.piooda.data.model

import com.google.firebase.Timestamp

data class NoticeItem (
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    var isExpanded: Boolean = false,
    val timestamp: Timestamp? = null
)