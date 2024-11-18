package com.piooda.data.repositoryImpl.mypage.detail.notice

import com.piooda.data.repository.mypage.detail.notice.NoticeRepository
import com.piooda.data.model.NoticeItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch


class NoticeRepositoryImpl(private val firestore: FirebaseFirestore): NoticeRepository {

    override fun getNotices(): Flow<UiState<List<NoticeItem>>> = callbackFlow {
        trySend(UiState.Loading)

        // FireStore 실시간 업데이트 리스너
        val listener = firestore.collection("notice")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val noticeList = snapshot.documents.mapNotNull { documents ->
                        documents.toObject(NoticeItem::class.java)
                    }
                    trySend(UiState.Success(noticeList))
                } else {
                    trySend(UiState.Empty)
                }
            }
        awaitClose {
            listener.remove()
        }
    }.catch { e ->
        emit(UiState.Error(e))
    }
}