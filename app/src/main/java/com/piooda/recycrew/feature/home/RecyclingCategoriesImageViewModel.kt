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

class RecyclingCategoriesImageViewModel(
    private var repository: ImageRepositoryImpl,
) : ViewModel() {

    private val _imageData = MutableStateFlow<List<ImageData>>(emptyList())
    val imageData: StateFlow<List<ImageData>> get() = _imageData

    fun loadImageData() {
        viewModelScope.launch {
            try {
                Log.d("RecyclingCategoriesImageViewModel", "Starting to fetch image data")
                repository.fetchImageData()
                    .onSuccess {
                        Log.d("RecyclingCategoriesImageViewModel", "Successfully fetched image data")
                        _imageData.emit(it)
                    }.onFailure { error ->
                        Log.e("RecyclingCategoriesImageViewModel", "Failed to fetch image data", error)
                    }
            } catch (e: CancellationException) {
                Log.e("RecyclingCategoriesImageViewModel", "Job was cancelled", e)
            } catch (e: Exception) {
                Log.e("RecyclingCategoriesImageViewModel", "Unexpected error occurred", e)
            }
        }
    }

}
