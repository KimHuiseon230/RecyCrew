package com.piooda.recycrew.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.domain.model.ImageData
import com.piooda.domain.repositoryImpl.ImageRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ImageViewModel(
    private var repository: ImageRepositoryImpl,
) : ViewModel() {

    private val _imageData = MutableStateFlow<List<ImageData>>(emptyList())
    val imageData: StateFlow<List<ImageData>> get() = _imageData

    fun loadImageData() {
        viewModelScope.launch {
            try {
                Log.d("ImageViewModel", "Starting to fetch image data")
                repository.fetchImageData()
                    .onSuccess {
                        Log.d("ImageViewModel", "Successfully fetched image data")
                        _imageData.emit(it)
                    }.onFailure { error ->
                        Log.e("ImageViewModel", "Failed to fetch image data", error)
                    }
            } catch (e: CancellationException) {
                Log.e("ImageViewModel", "Job was cancelled", e)
            } catch (e: Exception) {
                Log.e("ImageViewModel", "Unexpected error occurred", e)
            }
        }
    }

}
