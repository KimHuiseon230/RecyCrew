package com.piooda.data.repository.mypage.detail.editprofile

import android.net.Uri
import com.piooda.UiState
import kotlinx.coroutines.flow.Flow

interface EditProfileRepository {
    fun checkNicknameDuplicate(nickname: String): Flow<UiState<Boolean>>
    fun updateProfile(email: String, newNickname: String?, profilePictureUri: Uri?): Flow<UiState<Unit>>
}