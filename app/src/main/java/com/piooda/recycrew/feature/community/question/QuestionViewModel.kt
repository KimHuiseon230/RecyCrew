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

class QuestionViewModel(private val repository: PostDataRepository) : ViewModel() {

    private val _postData = MutableStateFlow<List<PostData>>(emptyList())
    val postData: StateFlow<List<PostData>> get() = _postData

    fun loadData() {
        viewModelScope.launch {
            try {
                Log.d("QuestionDetailsViewModel", "Starting to fetch post data")
                val result = repository.getAllPosts()
                result.onSuccess { postList ->
                    Log.d("QuestionDetailsViewModel", "Successfully fetched post data")
                    _postData.emit(postList)
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

}
