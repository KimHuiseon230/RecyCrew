package com.piooda.data.repository.mypage.detail.faq

import com.piooda.data.model.FAQItem
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.flow.Flow

interface FAQRepository {
    fun getFAQs(): Flow<UiState<List<FAQItem>>>
}