package com.piooda.recycrew.feature.mypage.detail.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.repository.mypage.detail.notification.NotificationRepository
import com.piooda.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository): ViewModel() {
    private val _notificationState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val notificationState get() = _notificationState.asStateFlow()

    private val _saveNotificationState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val saveNotificationState get() = _saveNotificationState.asStateFlow()

    fun getNotificationPreference() {
        viewModelScope.launch {
            notificationRepository.getNotificationPreference()
                .flowOn(Dispatchers.IO)
                .collect { state ->
                    _notificationState.value = state
                }
        }
    }

    fun saveNotificationPreference(isChecked: Boolean) {
        viewModelScope.launch {
            notificationRepository.saveNotificationPreference(isChecked)
                .flowOn(Dispatchers.IO)
                .collectLatest { state ->
                    _saveNotificationState.value = state
                }
        }
    }


}