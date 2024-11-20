package com.piooda.data.repository.mypage

import com.piooda.data.model.UserProfile
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.flow.Flow

interface MyPageRepository {
    fun loadUserProfile(): Flow<UiState<UserProfile>>
}