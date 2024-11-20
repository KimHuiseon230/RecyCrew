package com.piooda.data.repository.mypage.detail.notice

import com.piooda.data.model.NoticeItem
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {
    fun getNotices(): Flow<UiState<List<NoticeItem>>>
}