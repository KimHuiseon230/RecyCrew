package com.piooda.recycrew.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.domain.model.ImageData
import com.piooda.domain.repositoryImpl.ImageRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageViewModel(
    private var repository: ImageRepositoryImpl,
) : ViewModel() {

    private val _imageData = MutableStateFlow<List<ImageData>>(emptyList())
    val imageData: StateFlow<List<ImageData>> get() = _imageData

    fun loadImageData() {
        viewModelScope.launch {
            repository.fetchImageData()
                .onSuccess {
                    _imageData.emit(it)
                }.onFailure {

                }
        }
    }
}
