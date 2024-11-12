package com.piooda.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailedImageData(
    val title: String,
    val imageUrl: String,
    val num: Int,
    val subcategory: String,
    val excludedItems: String,
    val categoryLabel: String,
) : Parcelable