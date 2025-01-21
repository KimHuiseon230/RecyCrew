package com.piooda.data.repository.question

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.piooda.data.model.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class ContentUseCase @Inject constructor(
    private val contentRepository: ContentRepository, // ✅ 인터페이스 사용
) {

    fun loadList(): Flow<List<Content>> {
        return contentRepository.loadList() // Flow는 이미 비동기 스트림 처리 가능
    }

    // ✅ Firestore에 데이터 저장
    suspend fun save(item: Content): Boolean = try {
        contentRepository.insert(item)
    } catch (e: Exception) {
        false
    }

    suspend fun deletePost(postId: String): Boolean = try {
        contentRepository.delete(postId)
    } catch (e: Exception) {
        false
    }


    // ✅ 특정 게시글의 댓글 가져오기
    fun getCommentsForPost(postId: String): Flow<List<Content.Comment>> = flow {
        try {
            contentRepository.getCommentsForPost(postId).collect { comments ->
                emit(comments) // Flow 데이터를 안전하게 방출
            }
        } catch (e: Exception) {
            emit(emptyList()) // 예외 발생 시 빈 리스트 반환
        }
    }.flowOn(Dispatchers.IO) // I/O 스레드에서 실행


    // ✅ 특정 게시글에 댓글 추가
    suspend fun addCommentToPost(postId: String, comment: Content.Comment) {
        try {
            contentRepository.addCommentToPost(postId, comment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun toggleLike(contentId: String, uid: String) {
        try {
            contentRepository.toggleLike(contentId, uid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun observeContentList(): Flow<List<Content>> = callbackFlow {
        val firestore = FirebaseFirestore.getInstance()

        val listenerRegistration = firestore.collection("content")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "데이터 가져오기 실패: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val contents = snapshots?.documents?.mapNotNull { it.toObject(Content::class.java) }
                    ?: emptyList()
                Log.d("Firestore", "Firestore 데이터 수집 완료: ${contents.size}개")

                trySend(contents) // 최신 데이터를 Flow에 방출
            }

        awaitClose { listenerRegistration.remove() }
    }.flowOn(Dispatchers.IO)

}
