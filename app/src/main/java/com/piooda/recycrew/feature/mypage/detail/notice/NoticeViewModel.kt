package com.piooda.recycrew.feature.mypage.detail.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.repository.mypage.detail.notice.NoticeRepository
import com.piooda.data.model.NoticeItem
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class NoticeViewModel(private val noticeRepository: NoticeRepository): ViewModel() {
    private val _getNoticeState = MutableStateFlow<UiState<List<NoticeItem>>>(UiState.Loading)
    val getNoticeState get() = _getNoticeState.asStateFlow()

    fun getNotices() {
        viewModelScope.launch {
            noticeRepository.getNotices()
                .flowOn(Dispatchers.IO)
                .collectLatest { state ->
                    _getNoticeState.value = state
                }
        }
    }
}