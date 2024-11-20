package com.piooda.data.repository.mypage.detail.notification


import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationPreference(): Flow<UiState<Boolean>>
    fun saveNotificationPreference(isChecked: Boolean): Flow<UiState<Unit>>
}