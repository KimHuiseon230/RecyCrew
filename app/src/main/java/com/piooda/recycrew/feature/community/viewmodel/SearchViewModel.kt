package com.piooda.recycrew.feature.community.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.Content
import com.piooda.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


class SearchViewModel(private val repository: SearchRepository, private val context: Context) :
    ViewModel() {

    private val _searchResults = MutableStateFlow<List<Content>>(emptyList())
    val searchResults: StateFlow<List<Content>> = _searchResults.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isSearchViewExpanded = MutableStateFlow(false)
    val isSearchViewExpanded: StateFlow<Boolean> = _isSearchViewExpanded.asStateFlow()

    init {
        Log.d("SearchViewModel", "✅ SearchViewModel 초기화됨")
        loadSearchHistory()
    }
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
    // ✅ 검색 실행 (엔터 키 눌렀을 때만 실행)
    fun searchContent(query: String) {
        if (query.isBlank()) {
            Log.d("SearchViewModel", "⚠ 검색어가 비어 있어 실행되지 않음")
            return
        }

        Log.d("SearchViewModel", "🔥 검색 실행: $query")

        viewModelScope.launch {
            repository.searchContentRealtime(query)
                .onStart {
                    Log.d("SearchViewModel", "⏳ 검색 요청 시작: $query")
                }
                .catch { e ->
                    Log.e("SearchViewModel", "❌ 검색 중 오류 발생: ${e.message}")
                }
                .collectLatest { results ->
                    Log.d("SearchViewModel", "✅ 검색 결과 수신: ${results.size}개")
                    _searchResults.value = results // ✅ 검색 결과 RecyclerView 업데이트
                }
        }
    }

    // ✅ 검색 기록 저장
    fun addSearchHistory(query: String) {
        Log.d("SearchViewModel", "📜 검색 기록 추가: $query")

        repository.saveSearchHistory(query, context) // ✅ 검색 기록 저장
        loadSearchHistory() // ✅ 검색 기록 불러오기

        Log.d("SearchViewModel", "📜 검색 기록 추가 후 상태: ${_searchHistory.value}")
    }

    fun loadSearchHistory() {
        Log.d("SearchViewModel", "📜 검색 기록 로드")
        _searchHistory.value = repository.getSearchHistory(context)
    }

    fun deleteSearchQuery(query: String) {
        Log.d("SearchViewModel", "🗑 검색 기록 삭제 요청: $query")
        repository.deleteSearchHistory(query, context)  // 🔹 검색 기록 삭제
        loadSearchHistory()  // 🔹 최신 검색 기록 로드
    }

    fun setSearchViewExpanded(expanded: Boolean) {
        _isSearchViewExpanded.value = expanded
    }
}
