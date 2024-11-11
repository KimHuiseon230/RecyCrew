package com.piooda.recycrew.core_ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.domain.repositoryImpl.ImageRepositoryImpl
import com.piooda.recycrew.feature.home.RecyclingCategoriesImageViewModel

@Suppress("UNCHECKED_CAST")

val ViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        with(modelClass) {
            when {
                isAssignableFrom(RecyclingCategoriesImageViewModel::class.java) ->
                    RecyclingCategoriesImageViewModel(
                        ImageRepositoryImpl(
                            db = FirebaseFirestore.getInstance(),
                            firebaseStorage = FirebaseStorage.getInstance()
                        )
                    )

                else ->
                    throw IllegalArgumentException("Unknown ViewModel Class: ${modelClass.name}")
            } as T
        }
}