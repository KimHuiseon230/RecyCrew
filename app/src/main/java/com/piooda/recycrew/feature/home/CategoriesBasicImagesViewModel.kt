package com.piooda.recycrew.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.DetailedImageData
import com.piooda.data.repositoryImpl.FirebaseImageDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class CategoriesBasicImagesViewModel(
    private var repository: FirebaseImageDataRepository,
) : ViewModel() {

    private val _imageData = MutableStateFlow<List<DetailedImageData>>(emptyList())
    val imageData: StateFlow<List<DetailedImageData>> get() = _imageData

    fun loadImageData() {
        viewModelScope.launch {
            try {
                Log.d("CategoriesBasicImagesViewModel", "Starting to fetch image data")
                val result = repository.fetchBasicImagesData()
                result.onSuccess { imageList ->
                    Log.d("CategoriesBasicImagesViewModel", "Successfully fetched image data")
                    _imageData.emit(imageList)
                }.onFailure { error ->
                    Log.e("CategoriesBasicImagesViewModel", "Failed to fetch image data", error)
                }
            } catch (e: CancellationException) {
                Log.e("CategoriesBasicImagesViewModel", "Job was cancelled", e)
            } catch (e: Exception) {
                Log.e("CategoriesBasicImagesViewModel", "Unexpected error occurred", e)
            }
        }
    }
}
