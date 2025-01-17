package com.piooda.data.repositoryImpl.mypage.detail.faq

import com.piooda.data.repository.mypage.detail.faq.FAQRepository
import com.piooda.data.model.FAQItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.piooda.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch

class FAQRepositoryImpl(private val firestore: FirebaseFirestore) : FAQRepository {

    override fun getFAQs(): Flow<UiState<List<FAQItem>>> = callbackFlow {
        // 로딩 상태 방출
        trySend(UiState.Loading)

        // Firestore 실시간 업데이트 리스너
        val listener = firestore.collection("faq")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(UiState.Error(error))
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val faqList = snapshot.documents.mapNotNull { document ->
                    document.toObject(FAQItem::class.java)
                }
                trySend(UiState.Success(faqList))
            } else {
                trySend(UiState.Empty)
            }
        }

        // Flow가 종료될 때 리스너 제거
        awaitClose {
            listener.remove()
        }
    }.catch { e ->
        emit(UiState.Error(e))
    }
}