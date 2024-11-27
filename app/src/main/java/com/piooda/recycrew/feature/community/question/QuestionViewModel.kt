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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestionViewModel(private val repository: PostDataRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<PostData>>>(UiState.Loading)
    val state = _state.asStateFlow()

    fun loadData() {
        _state.update { UiState.Loading }
        viewModelScope.launch {
            repository.getAllPosts()
                .flowOn(Dispatchers.IO)
                .catch { e -> _state.value = UiState.Error(e) }
                .collectLatest { posts ->
                    _state.value = UiState.Success(posts)
                    Log.d("QuestionViewModel", "Post state updated: $posts")
                }
        }
    }
}
