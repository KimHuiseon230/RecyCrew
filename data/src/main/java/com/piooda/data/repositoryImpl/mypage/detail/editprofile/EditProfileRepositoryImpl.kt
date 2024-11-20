package com.piooda.data.repositoryImpl.mypage.detail.editprofile

import android.net.Uri
import com.piooda.data.repository.mypage.detail.editprofile.EditProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class EditProfileRepositoryImpl(private val firestore: FirebaseFirestore, private val storage: FirebaseStorage) :
    EditProfileRepository {
    override fun checkNicknameDuplicate(nickname: String): Flow<UiState<Boolean>> = flow {
        emit(UiState.Loading)

        try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .await()

            // 닉네임 중복 여부 반환
            val result = snapshot.documents.isNotEmpty() // 중복이 있으면 true, 없으면 false
            emit(UiState.Success(!result)) // 중복이 있으면 false 반환
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }

    override fun updateProfile(email: String, newNickname: String?, profilePictureUri: Uri?): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)

        try {
            val userDoc = firestore.collection("users").document(email)

            // 닉네임 업데이트
            newNickname?.let {
                userDoc.update("nickname", it).await()
            }

            // 프로필 사진 업데이트
            profilePictureUri?.let { uri ->
                val storageRef = storage.reference.child("profile_pictures/$email.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                userDoc.update("profilePicUrl", downloadUrl.toString()).await()
            }

            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }
}
