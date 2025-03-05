package com.piooda.recycrew.core

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.piooda.data.datasource.remote.PreferenceDataStoreManager
import com.piooda.data.repository.FirebaseImageDataRepository
import com.piooda.data.repository.SearchRepository
import com.piooda.data.repository.question.ContentRepository
import com.piooda.data.repositoryImpl.ContentRepositoryImpl
import com.piooda.data.repositoryImpl.SearchRepositoryImpl
import com.piooda.data.repositoryImpl.attendencecheck.AttendanceCheckRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.MyPageRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.editprofile.EditProfileRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.faq.FAQRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.notice.NoticeRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.notification.NotificationRepositoryImpl
import com.piooda.recycrew.feature.community.viewmodel.QuestionDetailsViewModel
import com.piooda.recycrew.feature.community.viewmodel.QuestionViewModel
import com.piooda.recycrew.feature.community.viewmodel.SearchViewModel
import com.piooda.recycrew.feature.event.viewmodel.AttendanceCheckViewModel
import com.piooda.recycrew.feature.home.viewmodle.CategoriesBasicImagesViewModel
import com.piooda.recycrew.feature.home.viewmodle.CategoriesDetailedImagesViewModel
import com.piooda.recycrew.feature.mypage.detail.editprofile.EditProfileViewModel
import com.piooda.recycrew.feature.mypage.detail.faq.FAQViewModel
import com.piooda.recycrew.feature.mypage.detail.notice.NoticeViewModel
import com.piooda.recycrew.feature.mypage.detail.notification.NotificationViewModel
import com.piooda.recycrew.feature.mypage.viewmodel.MyPageViewModel

@Suppress("UNCHECKED_CAST")

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private fun provideContentRepository(): ContentRepository {
        return ContentRepositoryImpl(firestore, firebaseStorage)
    }

    private val repository: ContentRepository by lazy { provideContentRepository() }

    val storage = Firebase.storage
    private val searchRepository: SearchRepository by lazy {
        SearchRepositoryImpl(firestore)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CategoriesBasicImagesViewModel::class.java) -> {
                CategoriesBasicImagesViewModel(
                    FirebaseImageDataRepository(firestore, firebaseStorage)
                ) as T
            }

            modelClass.isAssignableFrom(CategoriesDetailedImagesViewModel::class.java) -> {
                CategoriesDetailedImagesViewModel(
                    FirebaseImageDataRepository(firestore, firebaseStorage)
                ) as T
            }

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                Log.d("ViewModelFactory", " SearchViewModel 생성됨")  //  로그 추가
                return SearchViewModel(searchRepository, context) as T  //  Context 전달
            }

            modelClass.isAssignableFrom(QuestionViewModel::class.java) ->
                QuestionViewModel(repository, storage) as T

            modelClass.isAssignableFrom(NoticeViewModel::class.java) ->
                NoticeViewModel(
                    NoticeRepositoryImpl(firestore)
                ) as T

            modelClass.isAssignableFrom(QuestionDetailsViewModel::class.java) ->
                QuestionDetailsViewModel(repository) as T

            modelClass.isAssignableFrom(MyPageViewModel::class.java) ->
                MyPageViewModel(
                    MyPageRepositoryImpl(firebaseAuth)
                ) as T

            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                val dataStoreManager = PreferenceDataStoreManager(context)
                NotificationViewModel(
                    NotificationRepositoryImpl(dataStoreManager)
                ) as T
            }

            modelClass.isAssignableFrom(FAQViewModel::class.java) ->
                FAQViewModel(
                    FAQRepositoryImpl(firestore)
                ) as T

            modelClass.isAssignableFrom(EditProfileViewModel::class.java) -> {
                val storage = Firebase.storage
                EditProfileViewModel(
                    EditProfileRepositoryImpl(firestore, storage)
                ) as T
            }


            modelClass.isAssignableFrom(AttendanceCheckViewModel::class.java) ->
                AttendanceCheckViewModel(
                    AttendanceCheckRepositoryImpl(firestore)
                ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
