package com.piooda.recycrew.feature.mypage.detail.editprofile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.repository.mypage.detail.editprofile.EditProfileRepository
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class EditProfileViewModel(private val editProfileRepository: EditProfileRepository) : ViewModel() {
    private val _checkNicknameDuplicateState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val checkNicknameDuplicateState get() = _checkNicknameDuplicateState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri get() = _selectedImageUri.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val updateProfileState get() = _updateProfileState.asStateFlow()

    fun setSelectedImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }


    fun checkNicknameDuplicate(nickname: String) {
        viewModelScope.launch {
            editProfileRepository.checkNicknameDuplicate(nickname)
                .flowOn(Dispatchers.IO)
                .collect{ state ->
                    _checkNicknameDuplicateState.value = state
                 }
        }
    }

    fun updateProfile(email: String, newNickname: String?, profilePictureUri: Uri?) {
        viewModelScope.launch {
            editProfileRepository.updateProfile(email, newNickname, profilePictureUri)
                .flowOn(Dispatchers.IO)
                .collectLatest { state ->
                    _updateProfileState.value = state
                }
        }
    }
}