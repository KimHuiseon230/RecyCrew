package com.piooda.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserProfile(
    val name: String = "Unknown",
    val email: String = "No Email",
    val profilePicUrl: String? = null,
    var nickname: String? = null,
    var point: Int = 0
) : Parcelable