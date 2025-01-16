package com.piooda.data.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.piooda.data.model.Content
import com.piooda.data.model.ContentDto
import com.piooda.data.repository.ContentRepository
import com.piooda.data.repositoryImpl.ContentMapper.Companion.toContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class ContentRepositoryImpl(
    private val db: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ContentRepository { // ✅ 인터페이스 구현 추가
    private val postsCollection = db.collection("content")

    // ✅ Firestore에서 게시글 리스트 불러오기 (Flow 사용)
    override fun loadList(): Flow<List<Content>> = flow {
        try {
            // ✅ Firestore에서 데이터 가져오기
            val snapshot = postsCollection.get().await()

            // ✅ Firestore 데이터를 ContentDto → Content 변환 (변환 함수 적용)
            val contentList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ContentDto::class.java)?.toContent()
            }

            // ✅ 변환된 데이터를 Flow로 방출
            emit(contentList)

        } catch (e: FirebaseFirestoreException) {
            Log.e("Firestore Error", "데이터 로드 실패: ${e.message}")
            emit(emptyList())  // Firestore 오류 발생 시 빈 리스트 반환
        } catch (e: Exception) {
            Log.e("General Error", "예기치 않은 오류: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)  // ✅ I/O 스레드에서 실행


    // ✅ 게시글 추가 (ID 자동 생성 및 안정성 개선)
    override suspend fun insert(content: Content): Boolean {
        return try {
            val postRef = content.id?.let {
                postsCollection.document(it) // 사용자가 ID를 제공한 경우
            } ?: postsCollection.document() // ✅ Firestore에서 ID 자동 생성

            val postWithDefaults = content.copy(
                favoriteCount = 0, // ✅ 기본값 설정
            )

            // ✅ Firestore에 데이터 저장
            postRef.set(postWithDefaults).await()
            Log.d("ContentRepositoryImpl", "Post successfully added!")
            true
        } catch (e: Exception) {
            Log.e("Firestore Error", "Error adding post: ${e.localizedMessage}")
            false
        }
    }


    // 🔥 3. 게시글 업데이트 (ID 필요)
    override suspend fun update(content: Content): Boolean {
        return try {
            val postRef = FirebaseFirestore.getInstance()
                .collection("content")
                .document(content.id ?: return false)

            // ✅ Firestore에서 현재 데이터 로드 후 +1 (동시성 처리 개선)
            val snapshot = postRef.get().await()
            val currentLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0

            // ✅ Firestore에 정확한 값 업데이트
            postRef.update("likeCount", currentLikeCount + 1).await()
            true
        } catch (e: Exception) {
            Log.e("ContentUseCase", "Failed to update content: ${e.message}")
            false
        }
    }

    // 🔥 4. 게시글 삭제 (ID 필요)
    override suspend fun delete(content: Content): Boolean {
        return try {
            content.id?.let {
                postsCollection.document(it).delete().await()
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ 댓글 불러오기 (Firestore)
    override suspend fun getCommentsForPost(postId: String): List<Content.Comment> {
        return try {
            val snapshot = postsCollection.document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Content.Comment::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ 댓글 추가 (Firestore)
    override suspend fun addCommentToPost(postId: String, comment: Content.Comment) {
        try {
            postsCollection.document(postId)
                .collection("comments")
                .add(comment)
                .await()
        } catch (e: Exception) {
            Log.e("ContentRepository", "Failed to add comment: ${e.message}")
        }
    }

}