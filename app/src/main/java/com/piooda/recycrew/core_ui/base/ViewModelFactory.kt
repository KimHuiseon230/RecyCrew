package com.piooda.recycrew.core_ui.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.piooda.data.datastoreimpl.PreferenceDataStoreManager
import com.piooda.data.repositoryImpl.mypage.MyPageRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.editprofile.EditProfileRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.faq.FAQRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.notice.NoticeRepositoryImpl
import com.piooda.data.repositoryImpl.mypage.detail.notification.NotificationRepositoryImpl
import com.piooda.recycrew.feature.mypage.MyPageViewModel
import com.piooda.recycrew.feature.mypage.detail.editprofile.EditProfileViewModel
import com.piooda.recycrew.feature.mypage.detail.faq.FAQViewModel
import com.piooda.recycrew.feature.mypage.detail.notice.NoticeViewModel
import com.piooda.recycrew.feature.mypage.detail.notification.NotificationViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.piooda.data.repositoryImpl.FirebaseImageDataRepository
import com.piooda.data.repositoryImpl.PostDataRepositoryImpl
import com.piooda.data.repositoryImpl.attendencecheck.AttendanceCheckRepositoryImpl
import com.piooda.recycrew.feature.community.question.QuestionDetailsViewModel
import com.piooda.recycrew.feature.community.question.QuestionViewModel
import com.piooda.recycrew.feature.event.attendanceCheck.AttendanceCheckViewModel
import com.piooda.recycrew.feature.home.CategoriesBasicImagesViewModel
import com.piooda.recycrew.feature.home.CategoriesDetailedImagesViewModel

@Suppress("UNCHECKED_CAST")

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
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

                isAssignableFrom(MyPageViewModel::class.java) ->
                    MyPageViewModel(
                        MyPageRepositoryImpl(
                            firebaseAuth = FirebaseAuth.getInstance()
                        )
                    )

                isAssignableFrom(NotificationViewModel::class.java) -> {
                    val dataStoreManager = PreferenceDataStoreManager(context)
                    NotificationViewModel(
                        NotificationRepositoryImpl(dataStoreManager)
                    )
                }

                isAssignableFrom(FAQViewModel::class.java) -> {
                    val firestore = FirebaseFirestore.getInstance()
                    FAQViewModel(
                        FAQRepositoryImpl(firestore)
                    )
                }

                isAssignableFrom(NoticeViewModel::class.java) -> {
                    val firestore = FirebaseFirestore.getInstance()
                    NoticeViewModel(
                        NoticeRepositoryImpl(firestore)
                    )
                }

                isAssignableFrom(EditProfileViewModel::class.java) -> {
                    val firestore = FirebaseFirestore.getInstance()
                    val storage = Firebase.storage
                    EditProfileViewModel(
                        EditProfileRepositoryImpl(firestore, storage)
                    )
                }

                isAssignableFrom(AttendanceCheckViewModel::class.java) -> {
                    val firestore = FirebaseFirestore.getInstance()
                    AttendanceCheckViewModel(
                        AttendanceCheckRepositoryImpl(firestore)
                    )
                }
                else ->
                    throw IllegalArgumentException("Unknown ViewModel Class: ${modelClass.name}")
            } as T
        }
}
