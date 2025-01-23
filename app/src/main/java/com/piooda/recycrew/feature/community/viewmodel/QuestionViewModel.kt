package com.piooda.recycrew.feature.community.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.piooda.UiState
import com.piooda.data.model.Content
import com.piooda.data.repository.question.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuestionViewModel(
    private val repository: ContentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Content>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Content>>> = _state.asStateFlow()

    val contentList = repository.observeContentList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            contentList.collect {
                Log.d("ViewModel", "🔥 ViewModel에서 Flow 데이터 수집 완료: ${it.size}개")
            }
        }
    }
    // 🔹 상태를 수동으로 변경하는 메서드 추가
    fun setUiState(newState: UiState<List<Content>>) {
        _state.value = newState
    }
    fun toggleLike(content: Content) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.toggleLike(content.id ?: return@launch, uid)
                // 실시간 업데이트로 인해 별도의 상태 업데이트가 필요 없음
            } catch (e: Exception) {
                Log.e("toggleLike", "오류 발생: ${e.message}")
            }
        }
    }

    fun insert(content: Content) {
        viewModelScope.launch {
            try {
                repository.insert(content)
                // 실시간 업데이트로 인해 별도의 refreshPosts 호출이 필요 없음
            } catch (e: Exception) {
                _state.value = UiState.Error(e)
            }
        }
    }
}