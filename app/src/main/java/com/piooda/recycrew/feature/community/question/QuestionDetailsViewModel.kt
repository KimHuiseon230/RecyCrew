package com.piooda.recycrew.feature.community.question

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.domain.model.PostData
import com.piooda.domain.repository.PostDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class QuestionDetailsViewModel(private val repository: PostDataRepository) : ViewModel() {

    private val _q_postData = MutableStateFlow<List<PostData>>(emptyList())
    val q_postData: StateFlow<List<PostData>> get() = _q_postData

    fun loadData(postData: PostData) {
        viewModelScope.launch {
            try {
                Log.d("QuestionDetailsViewModel", "Starting to fetch post data")
                val result = repository.getAllPosts()
                result.onSuccess { postList ->
                    Log.d("QuestionDetailsViewModel", "Successfully fetched post data")
                    _q_postData.value = listOf(postData)
                }.onFailure { error ->
                    Log.e("QuestionDetailsViewModel", "Failed to fetch post data", error)
                }
            } catch (e: CancellationException) {
                Log.e("QuestionDetailsViewModel", "Job was cancelled", e)
            } catch (e: Exception) {
                Log.e("QuestionDetailsViewModel", "Unexpected error occurred", e)
            }
        }

    }

    fun getPostById(postData: PostData) {
        viewModelScope.launch {
            try {
                // 포스트 데이터 가져오기
                val post = repository.getPostById(postData.postId)
                _q_postData.value = listOf(post) // StateFlow에 결과 저장
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error fetching post by ID", e)
            }
        }
    }
}
