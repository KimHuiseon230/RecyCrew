package com.piooda.recycrew.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.DetailedImageData
import com.piooda.data.repositoryImpl.FirebaseImageDataRepository
import com.piooda.recycrew.core_ui.base.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class CategoriesDetailedImagesViewModel(
    private val repository: FirebaseImageDataRepository,
) : ViewModel() {

    // MutableStateFlow로 UI 상태 관리
    private val _detailedImageData = MutableStateFlow<UIState<List<DetailedImageData>>>(UIState.Loading)
    val detailedImageData: StateFlow<UIState<List<DetailedImageData>>> = _detailedImageData

    // 이미지 데이터를 로드하는 함수
    fun loadDetailedImageData() {
        viewModelScope.launch {
            try {
                // 로딩 상태로 변경
                _detailedImageData.value = UIState.Loading

                Log.d("CategoriesDetailedImagesViewModel", "Starting to fetch image data")
                val result = repository.fetchBasicImagesData()

                // 성공적으로 데이터를 가져오면 Success 상태로 업데이트
                result.onSuccess { data ->
                    Log.d("CategoriesDetailedImagesViewModel", "Successfully fetched image data")
                    _detailedImageData.value = UIState.Success(data)
                }.onFailure { error ->
                    // 실패 시 Error 상태로 업데이트
                    Log.e("CategoriesDetailedImagesViewModel", "Failed to fetch image data", error)
                    _detailedImageData.value = UIState.Failure("Failed to fetch data: ${error.message}")
                }
            } catch (e: CancellationException) {
                Log.e("CategoriesDetailedImagesViewModel", "Job was cancelled", e)
                _detailedImageData.value = UIState.Failure("Request was cancelled")
            } catch (e: Exception) {
                Log.e("CategoriesDetailedImagesViewModel", "Unexpected error occurred", e)
                _detailedImageData.value = UIState.Failure("Unexpected error occurred: ${e.message}")
            }
        }
    }
}