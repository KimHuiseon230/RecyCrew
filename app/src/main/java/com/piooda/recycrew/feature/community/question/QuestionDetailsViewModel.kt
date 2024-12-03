package com.piooda.recycrew.feature.community.question

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.Comment
import com.piooda.data.model.PostData
import com.piooda.data.repository.PostDataRepository
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class QuestionDetailsViewModel(private val repository: PostDataRepository) : ViewModel() {

    // StateFlow로 UI 상태 관리
    private val _postState = MutableStateFlow<UiState<PostData>>(UiState.Loading)
    val postState: StateFlow<UiState<PostData>> = _postState.asStateFlow()

    private val _commentsState = MutableStateFlow<UiState<List<Comment>>>(UiState.Loading)
    val commentsState: StateFlow<UiState<List<Comment>>> = _commentsState.asStateFlow()

    // 게시글 수정
    fun updatePost(postData: PostData) {
        viewModelScope.launch {
            repository.updatePost(postData)
                .flowOn(Dispatchers.IO)
                .onStart { _postState.value = UiState.Loading }
                .catch { e -> _postState.value = UiState.Error(e) }
                .collect { _postState.value = UiState.Success(postData) }
        }
    }

    // 게시글 삭제
    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
                .flowOn(Dispatchers.IO)
                .onStart { _postState.value = UiState.Loading }
                .catch { e -> _postState.value = UiState.Error(e) }
                .collect { _postState.value = UiState.Empty }
        }
    }

    // 게시글 로드
    private suspend fun loadPost(postId: String) {
        repository.getPostById(postId)
            .flowOn(Dispatchers.IO)
            .onStart { _postState.value = UiState.Loading }
            .catch { e -> _postState.value = UiState.Error(e) }
            .collectLatest { _postState.value = UiState.Success(it) }
    }

    // 댓글 로드
    private suspend fun loadComments(postId: String) {
        repository.getComments(postId)
            .flowOn(Dispatchers.IO)
            .onStart { _commentsState.value = UiState.Loading }
            .catch { e -> _commentsState.value = UiState.Error(e) }
            .collectLatest { _commentsState.value = UiState.Success(it) }
    }

    // 게시글과 댓글을 동시에 로드
    fun loadPostAndComments(postId: String) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    launch { loadPost(postId) }
                    launch { loadComments(postId) }
                }
            } catch (e: Exception) {
                Log.e("QuestionDetailsViewModel", "Error loading post and comments", e)
            }
        }
    }

    // 게시글 생성
    fun createPost(postData: PostData) {
        viewModelScope.launch {
            repository.createPost(postData)
                .flowOn(Dispatchers.IO)
                .onStart { _postState.value = UiState.Loading }
                .catch { e -> _postState.value = UiState.Error(e) }
                .collect { success ->
                    _postState.value = if (success) {
                        UiState.Success(postData)
                    } else {
                        UiState.Error(Exception("게시글 생성 실패"))
                    }
                }
        }
    }

    // 댓글 추가
    fun addCommentToPost(postId: String, comment: Comment) {
        viewModelScope.launch {
            repository.addCommentToPost(postId, comment)
                .flowOn(Dispatchers.IO)
                .onStart { _commentsState.value = UiState.Loading }
                .catch { e -> _commentsState.value = UiState.Error(e) }
                .collect {
                    loadComments(postId) // 댓글 리스트 새로고침
                }
        }
    }
}
