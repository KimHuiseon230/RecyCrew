package com.piooda.recycrew.feature.community.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.UiState
import com.piooda.data.model.Content
import com.piooda.data.repository.question.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuestionDetailsViewModel (
    private var repository: ContentRepository,
) : ViewModel() {

    private val _commentList = MutableStateFlow<List<Content.Comment>>(emptyList())
    val commentList: StateFlow<List<Content.Comment>> = _commentList.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _contentDetail = MutableStateFlow<Content?>(null)  // ✅ 게시물 정보 저장
    val contentDetail: StateFlow<Content?> = _contentDetail.asStateFlow()

    // 🔹 댓글 가져오기
    fun loadComments(postId: String) {
        viewModelScope.launch {
            repository.getCommentsForPost(postId).collect { comments ->
                _commentList.value = comments
            }
        }
    }

    // 🔹 댓글 추가
    fun addCommentToPost(postId: String, comment: Content.Comment) {
        viewModelScope.launch {
            repository.addCommentToPost(postId, comment)
            loadComments(postId) // 댓글 추가 후 다시 불러오기
        }
    }

    // 🔹 게시글 삭제
    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.delete(postId)
            _uiState.value = UiState.Success(Unit) // 삭제 후 UI 업데이트
        }
    }
    fun loadContentDetail(contentId: String) {
        viewModelScope.launch {
            repository.getContentById(contentId)
                .catch { e -> Log.e("DetailViewModel", "❌ 게시물 불러오기 실패: ${e.message}") }
                .collectLatest { content -> _contentDetail.value = content }
        }
    }
}
