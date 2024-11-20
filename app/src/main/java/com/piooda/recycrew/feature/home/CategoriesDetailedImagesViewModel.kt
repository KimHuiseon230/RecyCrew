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

class CategoriesDetailedImagesViewModel(
    private val repository: FirebaseImageDataRepository,
) : ViewModel() {
    private val _detailedImageData = MutableStateFlow<List<DetailedImageData>>(emptyList())
    val detailedImageData: StateFlow<List<DetailedImageData>> = _detailedImageData

    fun loadDetailedImageData(detailedImageData: DetailedImageData) {
        viewModelScope.launch {
            try {
                Log.d("CategoriesDetailedImagesViewModel", "Starting to fetch image data")
                val result = repository.fetchBasicImagesData()
                result.onSuccess {
                    Log.d("CategoriesDetailedImagesViewModel", "Successfully fetched image data")
                    _detailedImageData.value = listOf(detailedImageData)
                }.onFailure { error ->
                    Log.e("CategoriesDetailedImagesViewModel", "Failed to fetch image data", error)
                }
            } catch (e: CancellationException) {
                Log.e("CategoriesDetailedImagesViewModel", "Job was cancelled", e)
            } catch (e: Exception) {
                Log.e("CategoriesDetailedImagesViewModel", "Unexpected error occurred", e)
            }
        }
    }
}