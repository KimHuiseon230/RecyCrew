package com.piooda.recycrew.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.repository.mypage.MyPageRepository
import com.piooda.data.model.UserProfile
import com.piooda.UiState
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MyPageViewModel(private val myPageRepository: MyPageRepository) : ViewModel() {
    private val _loadUserProfileState = MutableStateFlow<UiState<UserProfile>>(UiState.Loading)
    val loadUserProfileState get() = _loadUserProfileState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            myPageRepository.loadUserProfile()
                .flowOn(Dispatchers.IO)
                .collectLatest { state ->
                    _loadUserProfileState.value = state
                    Log.d("MyPageViewModel", "User profile state updated: $state")
                }
        }
    }
}