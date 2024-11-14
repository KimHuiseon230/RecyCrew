package com.piooda.recycrew.core_ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.domain.repositoryImpl.FirebaseImageDataRepository
import com.piooda.domain.repositoryImpl.PostDataRepositoryImpl
import com.piooda.recycrew.feature.community.question.QuestionDetailsViewModel
import com.piooda.recycrew.feature.community.question.QuestionViewModel
import com.piooda.recycrew.feature.home.CategoriesBasicImagesViewModel
import com.piooda.recycrew.feature.home.CategoriesDetailedImagesViewModel

@Suppress("UNCHECKED_CAST")

val ViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        with(modelClass) {
            when {
                isAssignableFrom(CategoriesBasicImagesViewModel::class.java) ->
                    CategoriesBasicImagesViewModel(
                        FirebaseImageDataRepository(
                            db = FirebaseFirestore.getInstance(),
                            firebaseStorage = FirebaseStorage.getInstance()
                        )
                    )
                isAssignableFrom(CategoriesDetailedImagesViewModel::class.java) ->
                    CategoriesDetailedImagesViewModel(
                        FirebaseImageDataRepository(
                            db = FirebaseFirestore.getInstance(),
                            firebaseStorage = FirebaseStorage.getInstance()
                        )
                    )
                isAssignableFrom(QuestionViewModel::class.java) ->
                    QuestionViewModel(
                        PostDataRepositoryImpl(
                            db = FirebaseFirestore.getInstance(),
                            firebaseStorage = FirebaseStorage.getInstance()
                        )
                    )
                isAssignableFrom(QuestionDetailsViewModel::class.java) ->
                    QuestionDetailsViewModel(
                        PostDataRepositoryImpl(
                            db = FirebaseFirestore.getInstance(),
                            firebaseStorage = FirebaseStorage.getInstance()
                        )
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel Class: ${modelClass.name}")
            } as T
        }
}
