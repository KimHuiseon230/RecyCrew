package com.piooda.recycrew.feature.community.question

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.PostData
import com.piooda.data.repository.PostDataRepository
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestionViewModel(private val repository: PostDataRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<PostData>>>(UiState.Loading)
    val state = _state.asStateFlow()

    private val _likeState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val likeState = _likeState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.update { UiState.Loading }
            repository.getAllPosts()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _state.update { UiState.Error(e) }
                }
                .collect { posts ->
                    _state.update {
                        if (posts.isNotEmpty()) UiState.Success(posts)
                        else UiState.Empty
                    }
                    Log.d("QuestionViewModel", "Post state updated: $posts")
                }
        }
    }

    fun postdateLikeCount(postId: String, isLiked: Boolean) {
        viewModelScope.launch {
            _likeState.update { UiState.Loading }
            repository.postdateLikeCount(postId, isLiked)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _likeState.update { UiState.Error(e) }
                }
                .collect { success ->
                    // 좋아요 상태 업데이트 후 데이터 다시 로드
                    _likeState.update { UiState.Success(success) }
                    loadData() // 좋아요 후 전체 목록 새로고침
                }
        }
    }
}