package com.piooda.recycrew.feature.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.Content
import com.piooda.data.repository.ImageDataRepository
import com.piooda.data.repository.question.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class SearchViewModel @Inject constructor(
    private val imageDataRepository: ImageDataRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _allContentList = MutableStateFlow<List<Content>>(emptyList())
    val allContentList: StateFlow<List<Content>> = _allContentList.asStateFlow()

    private val _filteredList = MutableStateFlow<List<Content>>(emptyList())
    val filteredList: StateFlow<List<Content>> = _filteredList.asStateFlow()

    init {
        fetchAllContent()
    }

    private fun fetchAllContent() {
        viewModelScope.launch {
            val homeData = imageDataRepository.fetchBasicImagesData()
            val communityData = contentRepository.loadList()

            homeData.onSuccess { homeList ->
                communityData.collect { communityList ->
                    // 홈 데이터와 커뮤니티 데이터를 합쳐서 하나의 리스트로 관리
                    _allContentList.value = homeList.map {
                        Content(
                            id = it.num.toString(),
                            title = it.title,
                            content = "",  // 홈 데이터는 내용 없음
                            category = it.categoryLabel,
                            imagePath = it.imageUrl
                        )
                    } + communityList
                }
            }
        }
    }

    fun search(query: String) {
        _filteredList.value = if (query.isEmpty()) {
            _allContentList.value
        } else {
            _allContentList.value.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
    }
}