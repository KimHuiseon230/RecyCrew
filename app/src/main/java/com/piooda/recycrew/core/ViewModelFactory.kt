package com.piooda.recycrew.core

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.piooda.data.datasource.remote.PreferenceDataStoreManager
import com.piooda.data.repository.FirebaseImageDataRepository
import com.piooda.data.repository.question.ContentRepository
import com.piooda.data.repository.question.ContentUseCase
import com.piooda.data.repositoryImpl.ContentRepositoryImpl
import com.piooda.data.repositoryImpl.attendencecheck.AttendanceCheckRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.MyPageRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.editprofile.EditProfileRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.faq.FAQRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.notice.NoticeRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.notification.NotificationRepositoryImpl
import com.piooda.recycrew.feature.community.viewmodel.QuestionDetailsViewModel
import com.piooda.recycrew.feature.community.viewmodel.QuestionViewModel
import com.piooda.recycrew.feature.event.viewmodel.AttendanceCheckViewModel
import com.piooda.recycrew.feature.home.viewmodle.CategoriesBasicImagesViewModel
import com.piooda.recycrew.feature.home.viewmodle.CategoriesDetailedImagesViewModel
import com.piooda.recycrew.feature.mypage.detail.editprofile.EditProfileViewModel
import com.piooda.recycrew.feature.mypage.detail.faq.FAQViewModel
import com.piooda.recycrew.feature.mypage.detail.notice.NoticeViewModel
import com.piooda.recycrew.feature.mypage.detail.notification.NotificationViewModel
import com.piooda.recycrew.feature.mypage.viewmodel.MyPageViewModel

@Suppress("UNCHECKED_CAST")

// ✅ ViewModelFactory (싱글톤 Firebase 인스턴스 & DI 적용)
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    // ✅ Firebase 인스턴스 (싱글톤 유지)
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // ✅ ContentRepository 및 UseCase (인터페이스 기반 DI 적용)
    private val contentRepository: ContentRepository by lazy {
        ContentRepositoryImpl(firestore, firebaseStorage)
    }
    private val contentUseCase: ContentUseCase by lazy {
        ContentUseCase(contentRepository)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CategoriesBasicImagesViewModel::class.java) ->
                CategoriesBasicImagesViewModel(
                    FirebaseImageDataRepository(firestore, firebaseStorage)
                )

            modelClass.isAssignableFrom(CategoriesDetailedImagesViewModel::class.java) ->
                CategoriesDetailedImagesViewModel(
                    FirebaseImageDataRepository(firestore, firebaseStorage)
                )

            modelClass.isAssignableFrom(QuestionViewModel::class.java) ->
                QuestionViewModel(contentUseCase)

            modelClass.isAssignableFrom(QuestionDetailsViewModel::class.java) ->
                QuestionDetailsViewModel(contentUseCase)

            modelClass.isAssignableFrom(MyPageViewModel::class.java) ->
                MyPageViewModel(
                    MyPageRepositoryImpl(firebaseAuth)
                )

            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                val dataStoreManager = PreferenceDataStoreManager(context)
                NotificationViewModel(
                    NotificationRepositoryImpl(dataStoreManager)
                )
            }

            modelClass.isAssignableFrom(FAQViewModel::class.java) ->
                FAQViewModel(
                    FAQRepositoryImpl(firestore)
                )

            modelClass.isAssignableFrom(NoticeViewModel::class.java) ->
                NoticeViewModel(
                    NoticeRepositoryImpl(firestore)
                )

            modelClass.isAssignableFrom(EditProfileViewModel::class.java) -> {
                val storage = Firebase.storage
                EditProfileViewModel(
                    EditProfileRepositoryImpl(firestore, storage)
                )
            }

            modelClass.isAssignableFrom(AttendanceCheckViewModel::class.java) ->
                AttendanceCheckViewModel(
                    AttendanceCheckRepositoryImpl(firestore)
                )

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T
    }
}