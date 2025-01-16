package com.piooda.recycrew.feature.community.question

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.Content
import com.piooda.data.model.ContentDto

import com.piooda.data.repository.ContentUseCase
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuestionDetailsViewModel @Inject constructor(
    private val contentUseCase: ContentUseCase
) : ViewModel() {

    private val _commentList = MutableLiveData<List<Content.Comment>>()
    val commentList: LiveData<List<Content.Comment>> = _commentList

    // ✅ state의 제네릭 타입을 Unit으로 변경
    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val state: StateFlow<UiState<Unit>> = _state.asStateFlow()

    // ✅ 댓글 불러오기 (Firestore에서)
    fun loadComments(postId: String?) {
        if (postId == null) return
        viewModelScope.launch {
            try {
                val comments = contentUseCase.getCommentsForPost(postId) // ✅ 변환 후 호출
                _commentList.value = comments
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading comments: ${e.message}")
            }
        }
    }
    fun deletePost(content: Content?) {
        content?.let {
            performFirestoreOperation(
                operation = { contentUseCase.delete(content) },
                onSuccess = { Log.d("QuestionDetailsViewModel", "✅ Post successfully deleted.") },
                onError = { Log.e("QuestionDetailsViewModel", "Failed to delete post: ${it.message}") }
            )
        } ?: run {
            _state.value = UiState.Error(Exception("Invalid content data"))
        }
    }



    fun addCommentToPost(postId: String?, comment: Content.Comment) {
        if (postId == null) return
        viewModelScope.launch {
            contentUseCase.addCommentToPost(postId, comment)
            loadComments(postId)  // ✅ 댓글 추가 후 다시 로드
        }
    }


    // ✅ Firestore 요청을 공통 처리하는 메서드
    private fun <T> performFirestoreOperation(
        operation: suspend () -> T,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = { e ->
            Log.e(
                "QuestionDetailsViewModel",
                e.message.toString()
            )
        }
    ) {
        viewModelScope.launch {
            try {
                _state.value = UiState.Loading
                operation.invoke()
                _state.value = UiState.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                _state.value = UiState.Error(e)
                onError(e)
            }
        }
    }
}
