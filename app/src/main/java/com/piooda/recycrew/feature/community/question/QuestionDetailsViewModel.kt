package com.piooda.recycrew.feature.community.question

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.Comment
import com.piooda.data.model.PostData
import com.piooda.data.repository.PostDataRepository
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class QuestionDetailsViewModel(private val repository: PostDataRepository) : ViewModel() {
        // StateFlow로 UI 상태 관리
        private val _postState = MutableStateFlow<UiState<PostData>>(UiState.Loading)
        private val _commentsState = MutableStateFlow<UiState<List<Comment>?>>(UiState.Loading)
        val postState = _postState.asStateFlow()
        val commentsState = _commentsState.asStateFlow()

        // 게시글만 로드
        private suspend fun loadPost(postId: String) {
            repository.getPostById(postId)
                .flowOn(Dispatchers.IO)
                .catch { e -> _postState.value = UiState.Error(e) }
                .collectLatest { post ->
                    _postState.value = UiState.Success(post)
                }
        }
        // 댓글만 로드
        private suspend fun getComments(postId: String) {
            repository.getComments(postId)
                .flowOn(Dispatchers.IO)
                .catch { e -> _commentsState.value = UiState.Error(e) }
                .collectLatest { comments ->
                    _commentsState.value = UiState.Success(comments)
                }
        }

        // 게시글과 댓글을 동시에 로드하는 함수
        fun loadPostAndComments(postId: String) {
            viewModelScope.launch {
                try {
                    // 게시물과 댓글 로드를 동시에 처리
                    val postFlow = async { loadPost(postId) }
                    val commentsFlow = async { getComments(postId) }
                    postFlow.await()
                    commentsFlow.await() // 댓글 데이터도 처리되어야 함
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
                    .catch { e -> _postState.value = UiState.Error(e) }
                    .collect { isSuccessful ->
                        if (isSuccessful) {
                            _postState.value = UiState.Success(postData)
                        } else {
                            _postState.value = UiState.Error(Exception("게시글 생성 실패"))
                        }
                    }
            }
        }

    fun addCommentToPost(postId: String, comment: Comment) {
        viewModelScope.launch {
            repository.addCommentToPost(postId, comment)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _commentsState.value = UiState.Error(e)
                }
                .collect { updatedPost ->
                    _postState.value = UiState.Success(updatedPost)
                    fetchComments(postId)
                }
        }
    }

    fun fetchComments(postId: String) {
        viewModelScope.launch {
            repository.getComments(postId)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _commentsState.value = UiState.Error(e)
                }
                .collect { comments ->
                    // 댓글 상태 업데이트
                    _commentsState.value = UiState.Success(comments)
                }
        }
    }

}