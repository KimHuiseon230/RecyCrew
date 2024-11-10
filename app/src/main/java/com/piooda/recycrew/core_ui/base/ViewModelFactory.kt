package com.piooda.recycrew.core_ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.piooda.domain.repositoryImpl.ImageRepositoryImpl
import com.piooda.recycrew.feature.home.ImageViewModel

@Suppress("UNCHECKED_CAST")
val ViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        with(modelClass) {
            when {
                isAssignableFrom(ImageViewModel::class.java) ->
                    ImageViewModel(ImageRepositoryImpl())

                else ->
                    throw IllegalArgumentException("Unknown ViewModel Class: ${modelClass.name}")
            } as T
        }
}