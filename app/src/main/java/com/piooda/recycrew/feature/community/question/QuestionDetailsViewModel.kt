package com.piooda.recycrew.feature.community.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.PostData
import com.piooda.data.repository.PostDataRepository
import com.piooda.recycrew.core_ui.base.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestionDetailsViewModel(private val repository: PostDataRepository) : ViewModel() {

    private val _q_postData = MutableStateFlow<UIState<List<PostData>>>(UIState.Loading)
    val q_postData: StateFlow<UIState<List<PostData>>> get() = _q_postData

    // 게시물 목록을 불러오는 메서드
    fun loadData() {
        updateStateWithLoading {
            val result = repository.getAllPosts()
            handleResult(result,
                onSuccess = { postList -> _q_postData.value = UIState.Success(postList) },
                onFailure = { message -> _q_postData.value = UIState.Failure(message) }
            )
        }
    }

    // 게시물 삭제 메서드
    fun deletePost(postId: String) {
        updateStateWithLoading {
            // deletePost가 Result 타입을 반환하도록 변경
            val result = repository.deletePost(postId)
            handleResult(result,
                onSuccess = { _ ->
                    loadData() // UI 갱신
                },
                onFailure = { message ->
                    _q_postData.value = UIState.Failure(message) // 실패 메시지 처리
                }
            )
        }
    }

    // 게시물 생성 메서드
    fun createPost(postData: PostData) {
        updateStateWithLoading {
            val result = repository.createPost(postData)
            handleResult(result,
                onSuccess = { createdPost ->
                    val updatedList =
                        (_q_postData.value as? UIState.Success)?.data.orEmpty() + createdPost
                    _q_postData.value = UIState.Success(updatedList)
                },
                onFailure = { message -> _q_postData.value = UIState.Failure(message) }
            )
        }
    }

    // 로딩 상태 처리 간소화 함수
    private fun updateStateWithLoading(action: suspend () -> Unit) {
        viewModelScope.launch {
            _q_postData.value = UIState.Loading
            try {
                action()
            } catch (e: Exception) {
                _q_postData.value = UIState.Failure(e.message ?: "Unexpected error occurred")
            }
        }
    }
}

private fun <T> handleResult(
    result: Result<T>,
    onSuccess: (T) -> Unit = {},
    onFailure: (String) -> Unit = {},
) {
    result.onSuccess { data ->
        onSuccess(data)  // 성공적으로 데이터를 받았을 때의 처리
    }.onFailure { error ->
        val message = error.message ?: "Unknown error occurred"
        onFailure(message)  // 실패했을 때의 처리
    }
}
