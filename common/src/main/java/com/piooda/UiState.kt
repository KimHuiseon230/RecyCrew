package com.piooda

sealed class UiState<out T> {
    data object Empty : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val resultData: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
}
