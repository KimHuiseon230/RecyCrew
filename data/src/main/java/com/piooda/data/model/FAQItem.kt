package com.piooda.data.model

import com.google.firebase.Timestamp

data class FAQItem (
    val category: String = "",
    val question: String = "",
    val answer: String = "",
    var isExpanded: Boolean = false,
    val timestamp: Timestamp? = null
)
