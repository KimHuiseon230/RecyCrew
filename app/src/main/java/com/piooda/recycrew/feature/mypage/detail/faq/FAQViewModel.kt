package com.piooda.recycrew.feature.mypage.detail.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.repository.mypage.detail.faq.FAQRepository
import com.piooda.data.model.FAQItem
import com.piooda.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class FAQViewModel(private val faqRepository: FAQRepository): ViewModel() {
    private val _getFAQState = MutableStateFlow<UiState<List<FAQItem>>>(UiState.Loading)
    val getFAQState get() = _getFAQState.asStateFlow()

    fun getFAQs() {
        viewModelScope.launch {
            faqRepository.getFAQs()
                .flowOn(Dispatchers.IO)
                .collectLatest { state ->
                    _getFAQState.value = state
                }
        }
    }
}