package com.piooda.data.repositoryImpl.mypage.detail.notification

import com.piooda.data.datasource.remote.PreferenceDataStoreConstants
import com.piooda.data.datasource.remote.PreferenceDataStoreManager
import com.piooda.data.repository.mypage.detail.notification.NotificationRepository
import com.piooda.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class NotificationRepositoryImpl(private val dataStoreManager: PreferenceDataStoreManager) :
    NotificationRepository {
    override fun getNotificationPreference(): Flow<UiState<Boolean>> = flow {
        emit(UiState.Loading)
        try {
            val isPushChecked = dataStoreManager.readPreference(
                PreferenceDataStoreConstants.PUSH_NOTICE,
                false
            ).first()
            emit(UiState.Success(isPushChecked))
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }

    override fun saveNotificationPreference(isChecked: Boolean): Flow<UiState<Unit>> = flow {
        try {
            emit(UiState.Loading)
            dataStoreManager.createPreference(
                PreferenceDataStoreConstants.PUSH_NOTICE,
                isChecked
            )
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }
}
