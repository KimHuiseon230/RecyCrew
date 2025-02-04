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
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: SearchRepository,
    private val context: Context
) : ViewModel() {

    private val _searchResults =
        MutableStateFlow<List<Content>>(emptyList()) // 🔥 List<Content>로 변경
    val searchResults: StateFlow<List<Content>> = _searchResults.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList()) //  검색 기록
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _userSuggestions = MutableStateFlow<List<String>>(emptyList()) //  유저 검색어 자동완성
    val userSuggestions: StateFlow<List<String>> = _userSuggestions.asStateFlow()

    init {
        Log.d("SearchViewModel", " SearchViewModel 초기화됨")
        loadSearchHistory()
    }


    fun addSearchHistory(query: String) {
        repository.saveSearchHistory(query, context)
        loadSearchHistory()
    }

    fun loadSearchHistory() {
        _searchHistory.value = repository.getSearchHistory(context)
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _userSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            repository.searchUsers(query)
                .catch { e ->
                    Log.e("SearchViewModel", "❌ 유저 검색 중 오류 발생: ${e.message}")
                }
                .collectLatest { users ->
                    _userSuggestions.value = users.map { it.nickname ?: it.id.toString() }
                }
        }
    }

    fun searchContent(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            repository.searchContentRealtime(query)
                .catch { e ->
                    Log.e("SearchViewModel", "❌ 게시글 검색 중 오류 발생: ${e.message}")
                }
                .collectLatest { results ->
                    Log.d("SearchViewModel", "🔎 Firestore 검색 결과: ${results.size}개") //  데이터 개수 확인
                    results.forEach { Log.d("SearchViewModel", "📌 게시글: ${it.title}") }
                    _searchResults.value = results
                }
        }
    }


}
