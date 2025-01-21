package com.piooda.recycrew.feature.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.UiState
import com.piooda.data.model.Content
import com.piooda.data.repository.question.ContentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuestionDetailsViewModel @Inject constructor(
    private val contentUseCase: ContentUseCase,
) : ViewModel() {

    private val _commentList = MutableStateFlow<List<Content.Comment>>(emptyList())
    val commentList: StateFlow<List<Content.Comment>> = _commentList.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // 🔹 댓글 가져오기
    fun loadComments(postId: String) {
        viewModelScope.launch {
            contentUseCase.getCommentsForPost(postId).collect { comments ->
                _commentList.value = comments
            }
        }
    }

    // 🔹 댓글 추가
    fun addCommentToPost(postId: String, comment: Content.Comment) {
        viewModelScope.launch {
            contentUseCase.addCommentToPost(postId, comment)
            loadComments(postId) // 댓글 추가 후 다시 불러오기
        }
    }

    // 🔹 게시글 삭제
    fun deletePost(postId: String) {
        viewModelScope.launch {
            contentUseCase.deletePost(postId)
            _uiState.value = UiState.Success(Unit) // 삭제 후 UI 업데이트
        }
    }
}
