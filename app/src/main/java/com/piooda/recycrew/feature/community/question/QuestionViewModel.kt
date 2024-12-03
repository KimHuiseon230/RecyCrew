package com.piooda.recycrew.feature.community.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.PostData
import com.piooda.data.repository.PostDataRepository
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class QuestionViewModel(private val repository: PostDataRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<PostData>>>(UiState.Loading)
    val state: StateFlow<UiState<List<PostData>>> = _state.asStateFlow()

    private val _likeState = MutableStateFlow<UiState<Boolean>>(UiState.Empty)
    val likeState: StateFlow<UiState<Boolean>> = _likeState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            repository.getAllPosts()
                .flowOn(Dispatchers.IO)
                .onStart { _state.value = UiState.Loading }
                .catch { e -> _state.value = UiState.Error(e) }
                .collect { posts ->
                    _state.value = if (posts.isNotEmpty()) {
                        UiState.Success(posts)
                    } else {
                        UiState.Empty
                    }
                }
        }
    }

    fun postdateLikeCount(postId: String, isLiked: Boolean) {
        viewModelScope.launch {
            repository.postdateLikeCount(postId, isLiked)
                .flowOn(Dispatchers.IO)
                .onStart { _likeState.value = UiState.Loading }
                .catch { e -> _likeState.value = UiState.Error(e) }
                .collect { success ->
                    _likeState.value = UiState.Success(success)
                    loadData() // 좋아요 처리 후 데이터 새로고침
                }
        }
    }
}
