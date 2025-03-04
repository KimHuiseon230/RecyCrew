package com.piooda.data.repositoryImpl.mypage

import com.google.firebase.auth.FirebaseAuth
import com.piooda.data.repository.mypage.MyPageRepository
import com.piooda.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.piooda.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MyPageRepositoryImpl(private val firebaseAuth: FirebaseAuth): MyPageRepository {

    override fun loadUserProfile(): Flow<UiState<UserProfile>> = callbackFlow {
        trySend(UiState.Loading)
        val user = firebaseAuth.currentUser

        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val email = user.email ?: return@callbackFlow
            val userDoc = db.collection("users").document(email)

            userDoc.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userProfile = document.toObject(UserProfile::class.java) ?: UserProfile()
                        trySend(UiState.Success(userProfile))
                    } else {
                        // 문서가 존재하지 않으면 Empty 상태 전송
                        trySend(UiState.Empty)
                    }
                    close() // 콜백 종료
                }
                .addOnFailureListener { e ->
                    // 실패 시 에러 상태 전송
                    trySend(UiState.Error(e))
                    close()
                }
        } else {
            // 사용자 정보가 없을 경우 에러 전송
            trySend(UiState.Error(Exception("User not logged in")))
            close()
        }

        awaitClose() // Flow 종료 처리
    }
}