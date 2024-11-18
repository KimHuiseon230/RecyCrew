package com.piooda.recycrew.core_ui.base

sealed interface UIState<out T> {

    data object Loading : UIState<Nothing>

    data class Success<T>(
        val data: T
    ) : UIState<T>

    data class Failure(
        val errorMessage: String
    ) : UIState<Nothing>
}

